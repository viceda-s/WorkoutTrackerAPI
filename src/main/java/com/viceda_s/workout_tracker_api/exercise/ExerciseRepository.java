package com.viceda_s.workout_tracker_api.exercise;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long>{
    List<Exercise> findByType(ExerciseType type);
    List<Exercise> findByMuscleGroup(MuscleGroup muscleGroup);
}
