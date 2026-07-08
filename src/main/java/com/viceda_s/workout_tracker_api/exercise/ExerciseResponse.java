package com.viceda_s.workout_tracker_api.exercise;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExerciseResponse {
    
    @Schema(example="1")
    private Long id;

    @Schema(example="Bench Press")
    private String name;

    @Schema(example="A compound chest exercise performed lying on a bench, pressing a barbell upward.")
    private String description;

    @Schema(example="STRENGTH")
    private ExerciseType type;

    @Schema(example="LEGS")
    private MuscleGroup muscleGroup;
}
