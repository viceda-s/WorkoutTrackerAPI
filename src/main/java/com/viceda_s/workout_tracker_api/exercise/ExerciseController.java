package com.viceda_s.workout_tracker_api.exercise;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viceda_s.workout_tracker_api.exercise.dto.ExerciseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Exercises", description = "Browse the shared exercise library (public, no authentication required)")
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;

    private ExerciseResponse toResponse(Exercise exercise) {
        return new ExerciseResponse(
            exercise.getId(), 
            exercise.getName(), 
            exercise.getDescription(), 
            exercise.getType(), 
            exercise.getMuscleGroup()
        );
    }

    @Operation(summary = "List exercises",
            description = "Returns all exercises, optionally filtered by type or muscle group. If both are supplied, exerciseType takes precedence.")
    @ApiResponse(responseCode = "200", description = "Exercises returned successfully")
    @GetMapping
    public List<ExerciseResponse> getExercises(
        @Parameter(description = "Filter by exercise type", example = "CARDIO")
        @RequestParam(required=false) ExerciseType exerciseType,
        @Parameter(description = "Filter by muscle group", example = "LEGS")
        @RequestParam(required=false) MuscleGroup muscleGroup
    ) {
        List<Exercise> exercises;
        if (exerciseType != null) {
            exercises = exerciseService.getExercisesByType(exerciseType);
        } else if (muscleGroup != null) {
            exercises = exerciseService.getExerciseByMuscleGroup(muscleGroup);
        } else {
            exercises = exerciseService.getAllExercises();
        }
        return exercises.stream().map(this::toResponse).toList();
    }

    @Operation(summary = "Get a single exercise by id")
    @ApiResponse(responseCode = "200", description = "Exercise found")
    @ApiResponse(responseCode = "404", description = "No exercise exists with that id")
    @GetMapping("/{id}")
    public ExerciseResponse getExerciseById(@PathVariable Long id) {
        return toResponse(exerciseService.getExerciseById(id));
    }
}
