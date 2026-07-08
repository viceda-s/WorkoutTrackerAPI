package com.viceda_s.workout_tracker_api.workout;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.workout.dto.ExerciseVolumeSummary;

public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
    @Query("SELECT new com.viceda_s.workout_tracker_api.workout.dto.ExerciseVolumeSummary(we.exercise.name, SUM(we.sets * we.reps * we.weight)) " +
           "FROM WorkoutExercise we " +
           "WHERE we.workoutPlan.owner = :owner " +
           "AND we.workoutPlan.status = 'COMPLETED' " +
           "AND we.workoutPlan.scheduledAt BETWEEN :from AND :to " +
           "GROUP BY we.exercise.name")

    List<ExerciseVolumeSummary> summarizeVolumeByOwnerAndPeriod(
            @Param("owner") User owner,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
