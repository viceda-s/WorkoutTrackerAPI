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
        owner.setEmail("alice@example.com");

        Exercise exercise = new Exercise();
        exercise.setId(10L);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(10L)).thenReturn(Optional.of(exercise));
        
        workoutService.createWorkout("alice@example.com", buildRequest(10L));

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
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> workoutService.createWorkout("alice@example.com", buildRequest(999L)));
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
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));

        workoutService.listWorkouts("alice@example.com", null);

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
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));

        workoutService.listWorkouts("alice@example.com", WorkoutStatus.PLANNED);

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
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
            () -> workoutService.getWorkoutById("alice@example.com", 1L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    /**
     * Updating a workout that doesn't belong to the caller should be
     * rejected with a 404.
     */
    @Test
    void updateWorkout_NotOwned_ThrowsNotFound() {
        User owner = new User();
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () 
                -> workoutService.updateWorkout("alice@example.com", 1L, buildRequest(10L)));
    }

    /**
     * Deleting a workout that doesn't belong to the caller should be
     * rejected with a 404, and should never reach the point of deleting
     * anything.
     */
    @Test
    void deleteWorkout_NotOwned_ThrowsNotFound() {
        User owner = new User();
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, ()
                -> workoutService.deleteWorkout("alice@example.com", 1L));
        verify(workoutPlanRepository, never()).delete(any());
    }

    /**
     * Updating a workout's exercises should replace the existing list
     * entirely, not append the new exercises alongside the old ones.
     */
    @Test
    void updateWorkout_ReplacesExercisesNotAppends() {
        User owner = new User();
        owner.setEmail("alice@example.com");

        Exercise oldExercise = new Exercise();
        oldExercise.setId(10L);
        WorkoutExercise oldWorkoutExercise = new WorkoutExercise();
        oldWorkoutExercise.setExercise(oldExercise);

        WorkoutPlan existingPlan = new WorkoutPlan();
        existingPlan.setOwner(owner);
        existingPlan.setExercises(new ArrayList<>(List.of(oldWorkoutExercise)));

        Exercise newExercise = new Exercise();
        newExercise.setId(20L);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(workoutPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(existingPlan));
        when(exerciseRepository.findById(20L)).thenReturn(Optional.of(newExercise));

        workoutService.updateWorkout("alice@example.com", 1L, buildRequest(20L));
        
        assertEquals(1, existingPlan.getExercises().size());
        assertEquals(newExercise, existingPlan.getExercises().get(0).getExercise());
    }
}