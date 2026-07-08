package com.viceda_s.workout_tracker_api.workout.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkoutExerciseResponse {
    @Schema(example = "1")
    private Long exerciseId;

    @Schema(example = "Bench Press")
    private String exerciseName;

    @Schema(example = "4")
    private Integer sets;

    @Schema(example = "8")
    private Integer reps;

    @Schema(example = "60.0")
    private BigDecimal weight;

    @Schema(example = "0")
    private Integer orderIndex;
}

