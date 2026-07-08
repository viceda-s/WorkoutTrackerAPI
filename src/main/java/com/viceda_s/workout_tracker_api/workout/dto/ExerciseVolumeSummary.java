package com.viceda_s.workout_tracker_api.workout.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Value;

@Value
public class ExerciseVolumeSummary {
    @Schema(example = "Bench Press")
    String exerciseName;

    @Schema(example = "1600.0")
    BigDecimal totalVolume;
}
