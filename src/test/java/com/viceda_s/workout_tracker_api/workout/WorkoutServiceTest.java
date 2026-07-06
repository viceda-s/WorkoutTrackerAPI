package com.viceda_s.workout_tracker_api.workout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.viceda_s.workout_tracker_api.exercise.Exercise;
import com.viceda_s.workout_tracker_api.exercise.ExerciseRepository;
import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.user.UserRepository;

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

    @Test
    void createWorkout_SavesWithCorrecrOwnerAndExercise() {
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

    @Test
    void createWorkout_UnknownExerciseId_ThrowsAndNeverSaves() {
        User owner = new User();
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));
        when(exerciseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> workoutService.createWorkout("alice@example.com", buildRequest(999L)));

        verify(workoutPlanRepository, never()).save(any());
    }

    @Test
    void listWorkouts_NoStatus_CallsFindByOwnerOnly() {
        User owner = new User();
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));

        workoutService.listWorkouts("alice@example.com", null);

        verify(workoutPlanRepository).findByOwnerOrderByScheduledAtAsc(owner);
        verify(workoutPlanRepository, never()).findByOwnerAndStatusOrderByScheduledAtAsc(any(), any());
    }

    @Test
    void listWorkouts_WithStatus_CallsFindByOwnerAndStatus() {
        User owner = new User();
        owner.setEmail("alice@example.com");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(owner));

        workoutService.listWorkouts("alice@example.com", WorkoutStatus.PLANNED);

        verify(workoutPlanRepository).findByOwnerAndStatusOrderByScheduledAtAsc(owner, WorkoutStatus.PLANNED);
        verify(workoutPlanRepository, never()).findByOwnerOrderByScheduledAtAsc(any());
    }
}
