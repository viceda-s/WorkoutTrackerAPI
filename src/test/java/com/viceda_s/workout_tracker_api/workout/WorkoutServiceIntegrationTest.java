package com.viceda_s.workout_tracker_api.workout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.viceda_s.workout_tracker_api.BaseIntegrationTest;
import com.viceda_s.workout_tracker_api.exercise.Exercise;
import com.viceda_s.workout_tracker_api.exercise.ExerciseRepository;
import com.viceda_s.workout_tracker_api.exercise.ExerciseType;
import com.viceda_s.workout_tracker_api.exercise.MuscleGroup;
import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.user.UserRepository;
import com.viceda_s.workout_tracker_api.workout.dto.CreateWorkoutRequest;

public class WorkoutServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WorkoutService workoutService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Test
    void createWorkout_PersiststoRealDatabase() {
        User user = new User();
        user.setEmail("integration@test.com");
        user.setName("Integration Tester");
        user.setPassword("securepassword");
        userRepository.save(user);

        Exercise exercise = new Exercise();
        exercise.setName("Deadlift");
        exercise.setType(ExerciseType.STRENGTH);
        exercise.setMuscleGroup(MuscleGroup.BACK);
        exercise.setDescription("A compound weight training exercise where a loaded barbell is lifted off the ground to the level of the hips.");
        exercise = exerciseRepository.save(exercise);

        CreateWorkoutRequest.ExerciseLine line = new CreateWorkoutRequest.ExerciseLine();
        line.setExerciseId(exercise.getId());
        line.setSets(3);
        line.setReps(10);
        line.setWeight(new BigDecimal("100.0"));

        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Heavy Back Day");
        request.setScheduledAt(Instant.now());
        request.setExercises(List.of(line));

        WorkoutPlan savedPlan = workoutService.createWorkout("integration@test.com", request);

        assertNotNull(savedPlan.getId(), "The WorkoutPlan should have received a real ID from the DB");
        assertEquals("Heavy Back Day", savedPlan.getName());
        assertEquals(1, savedPlan.getExercises().size());
        assertEquals("Deadlift", savedPlan.getExercises().get(0).getExercise().getName());
    }
}
