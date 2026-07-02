package com.viceda_s.workout_tracker_api.exercise;

import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;
    
    @GetMapping
    public List<Exercise> getExercises(
        @RequestParam(required=false) ExerciseType exerciseType,
        @RequestParam(required=false) MuscleGroup muscleGroup
    ) {
        if (exerciseType != null) {
            return exerciseService.getExercisesByType(exerciseType);
        } else if (muscleGroup != null) {
            return exerciseService.getExerciseByMuscleGroup(muscleGroup);
        } else {
            return exerciseService.getAllExercises();
        }
    }

    @GetMapping("/{id}")
    public Exercise getExerciseById(@PathVariable Long id) {
        return exerciseService.getExerciseById(id);
    }
}
