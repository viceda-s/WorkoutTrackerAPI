package com.viceda_s.workout_tracker_api.workout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.viceda_s.workout_tracker_api.exercise.Exercise;
import com.viceda_s.workout_tracker_api.exercise.ExerciseRepository;
import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.user.UserRepository;

/**
 * Unit tests for {@link WorkoutService}, covering: workout creation (correct
 * ownership and exercise linkage, and rejection of unknown exercise ids);
 * listing workouts with and without a status filter; ownership enforcement
 * on fetching, updating, and deleting a workout (rejecting non-owners with
 * a 404, without distinguishing "doesn't exist" from "not yours"); and that
 * updating a workout's exercises replaces the existing list rather than
 * appending to it.
 */
@ExtendWith(MockitoExtension.class)
public class WorkoutServiceTest {
    
    @Mock
    private WorkoutPlanRepository workoutPlanRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutExerciseRepository workoutExerciseRepository;

    @InjectMocks
    private WorkoutService workoutService;

    private CreateWorkoutRequest buildRequest(Long exerciseId) {
        CreateWorkoutRequest.ExerciseLine line = new CreateWorkoutRequest.ExerciseLine();
        line.setExerciseId(exerciseId);
        line.setSets(3);
        line.setReps(10);
        line.setWeight(new BigDecimal("50.0"));

        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Leg Day");
        request.setScheduledAt(Instant.now());
        request.setExercises(List.of(line));
        return request;
    }

    /**
     * Creating a workout should save it linked to the resolved owner and
     * with its exercise lines correctly attached.
     */
    @Test
    void createWorkout_SavesWithCorrectOwnerAndExercise() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        Exercise exercise = new Exercise();
        exercise.setId(10L);

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(10L)).thenReturn(Optional.of(exercise));
        
        workoutService.createWorkout("vince@example.com", buildRequest(10L));

        ArgumentCaptor<WorkoutPlan> captor = ArgumentCaptor.forClass(WorkoutPlan.class);
        verify(workoutPlanRepository).save(captor.capture());

        WorkoutPlan savePlan = captor.getValue();
        assertEquals(owner, savePlan.getOwner());
        assertEquals(1, savePlan.getExercises().size());
        assertEquals(exercise, savePlan.getExercises().get(0).getExercise());
    }

    /**
     * Creating a workout that references an exercise id which doesn't exist
     * should be rejected with a 400 Bad Request, and should never reach the
     * point of saving anything.
     */
    @Test
    void createWorkout_UnknownExerciseId_ThrowsAndNeverSaves() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> workoutService.createWorkout("vince@example.com", buildRequest(999L)));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(workoutPlanRepository, never()).save(any());
    }

    /**
     * Listing workouts without a status filter should query for all of the
     * owner's workouts, not the status-filtered variant.
     */
    @Test
    void listWorkouts_NoStatus_CallsFindByOwnerOnly() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));

        workoutService.listWorkouts("vince@example.com", null);

        verify(workoutPlanRepository).findByOwnerOrderByScheduledAtAsc(owner);
        verify(workoutPlanRepository, never()).findByOwnerAndStatusOrderByScheduledAtAsc(any(), any());
    }

    /**
     * Listing workouts with a status filter should query using that status,
     * not the unfiltered variant.
     */
    @Test
    void listWorkouts_WithStatus_CallsFindByOwnerAndStatus() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));

        workoutService.listWorkouts("vince@example.com", WorkoutStatus.PLANNED);

        verify(workoutPlanRepository).findByOwnerAndStatusOrderByScheduledAtAsc(owner, WorkoutStatus.PLANNED);
        verify(workoutPlanRepository, never()).findByOwnerOrderByScheduledAtAsc(any());
    }

    /**
     * Fetching a workout that doesn't belong to the caller (or doesn't exist
     * at all) should be rejected with a 404, not distinguishing between the
     * two cases.
    */
    @Test
    void getWorkoutById_NotOwned_ThrowsNotFound() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> workoutService.getWorkoutById("vince@example.com", 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    /**
     * Updating a workout that doesn't belong to the caller should be
     * rejected with a 404.
     */
    @Test
    void updateWorkout_NotOwned_ThrowsNotFound() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () 
                -> workoutService.updateWorkout("vince@example.com", 1L, buildRequest(10L)));
    }

    /**
     * Deleting a workout that doesn't belong to the caller should be
     * rejected with a 404, and should never reach the point of deleting
     * anything.
     */
    @Test
    void deleteWorkout_NotOwned_ThrowsNotFound() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, ()
                -> workoutService.deleteWorkout("vince@example.com", 1L));
        verify(workoutPlanRepository, never()).delete(any());
    }

    /**
     * Updating a workout's exercises should replace the existing list
     * entirely, not append the new exercises alongside the old ones.
     */
    @Test
    void updateWorkout_ReplacesExercisesNotAppends() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        Exercise oldExercise = new Exercise();
        oldExercise.setId(10L);
        WorkoutExercise oldWorkoutExercise = new WorkoutExercise();
        oldWorkoutExercise.setExercise(oldExercise);

        WorkoutPlan existingPlan = new WorkoutPlan();
        existingPlan.setOwner(owner);
        existingPlan.setExercises(new ArrayList<>(List.of(oldWorkoutExercise)));

        Exercise newExercise = new Exercise();
        newExercise.setId(20L);

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(existingPlan));
        when(exerciseRepository.findById(20L)).thenReturn(Optional.of(newExercise));

        workoutService.updateWorkout("vince@example.com", 1L, buildRequest(20L));
        
        assertEquals(1, existingPlan.getExercises().size());
        assertEquals(newExercise, existingPlan.getExercises().get(0).getExercise());
    }

    /**
     * Generating a progress report should combine the completed-workout
     * count and per-exercise volume summary from their respective
     * repositories into one response.
     */
    @Test
    void generateProgressReport_AssemblesCountAndVolumes() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-31T00:00:00Z");

        List<ExerciseVolumeSummary> volumes = List.of(new ExerciseVolumeSummary("Bench Press", new BigDecimal("700.0")));

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.countByOwnerAndStatusAndScheduledAtBetween(owner, WorkoutStatus.COMPLETED, from, to))
                .thenReturn(2L);
        when(workoutExerciseRepository.summarizeVolumeByOwnerAndPeriod(owner, from, to))
                .thenReturn(volumes);
        
        ProgressReportResponse report = workoutService.generateProgressReport("vince@example.com", from, to);
        assertEquals(2L, report.getTotalCompletedWorkouts());
        assertEquals(volumes, report.getExerciseVolumes());
    }

    /**
     * The completed-workout count must always be queried specifically for
     * COMPLETED status, not left to default to something else.
     */
    @Test
    void generateProgressReport_CountsOnlyCompletedStatus() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-31T00:00:00Z");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.countByOwnerAndStatusAndScheduledAtBetween(any(), any(), any(), any()))
                .thenReturn(0L);
        when(workoutExerciseRepository.summarizeVolumeByOwnerAndPeriod(any(), any(), any()))
                .thenReturn(List.of());
        
        workoutService.generateProgressReport("vince@example.com", from, to);

        verify(workoutPlanRepository).countByOwnerAndStatusAndScheduledAtBetween(owner, WorkoutStatus.COMPLETED, from, to);
    }

    /**
     * Changing a workout's status should update and save it when the
     * caller owns the plan.
     */
    @Test
    void updateStatus_ChangesAndSaves() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        WorkoutPlan plan = new WorkoutPlan();
        plan.setOwner(owner);
        plan.setStatus(WorkoutStatus.PLANNED);

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(plan));

        workoutService.updateStatus("vince@example.com", 1L, WorkoutStatus.COMPLETED);

        assertEquals(WorkoutStatus.COMPLETED, plan.getStatus());
        verify(workoutPlanRepository).save(plan);
    }

    /**
     * Changing the status of a workout that doesn't belong to the caller
     * should be rejected with a 404.
     */
    @Test
    void updateStatus_NotOwned_ThrowsNotFound() {
        User owner = new User();
        owner.setEmail("vince@example.com");

        when(userRepository.findByEmail("vince@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> workoutService.updateStatus("vince@example.com", 1L, WorkoutStatus.COMPLETED));
    }

}