package com.viceda_s.workout_tracker_api.workout.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateWorkoutRequest {

    @NotBlank
    @Schema(example = "Push Day")
    private String name;

    @NotNull
    @Schema(example = "2026-07-10T09:00:00Z")
    private Instant scheduledAt;

    @NotEmpty
    @Valid
    private List<ExerciseLine> exercises;

    @Data
    public static class ExerciseLine {
        @NotNull
        @Schema(example = "1")
        private Long exerciseId;

        @NotNull
        @Schema(example = "4")
        private Integer sets;

        @NotNull
        @Schema(example = "8")
        private Integer reps;
        
        @NotNull
        @Schema(example = "60.0")
        private BigDecimal weight;
    }
}
