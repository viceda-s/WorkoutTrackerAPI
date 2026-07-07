package com.viceda_s.workout_tracker_api.exercise;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Exercises", description = "Browse the shared exercise library (public, no authentication required)")
@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {
    private final ExerciseService exerciseService;

    @Operation(summary = "List exercises",
            description = "Returns all exercises, optionally filtered by type or muscle group. If both are supplied, exerciseType takes precedence.")
    @ApiResponse(responseCode = "200", description = "Exercises returned successfully")
    @GetMapping
    public List<Exercise> getExercises(
        @Parameter(description = "Filter by exercise type", example = "CARDIO")
        @RequestParam(required=false) ExerciseType exerciseType,
        @Parameter(description = "Filter by muscle group", example = "LEGS")
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

    @Operation(summary = "Get a single exercise by id")
    @ApiResponse(responseCode = "200", description = "Exercise found")
    @ApiResponse(responseCode = "404", description = "No exercise exists with that id")
    @GetMapping("/{id}")
    public Exercise getExerciseById(@PathVariable Long id) {
        return exerciseService.getExerciseById(id);
    }
}
