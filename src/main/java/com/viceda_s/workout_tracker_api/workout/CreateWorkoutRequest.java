package com.viceda_s.workout_tracker_api.workout;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateWorkoutRequest {

    @NotBlank
    private String name;

    private Instant scheduledAt;

    @NotEmpty
    private List<ExerciseLine> exercises;

    @Data
    public static class ExerciseLine {
        private Long exerciseId;
        private Integer sets, reps;
        private BigDecimal weight;
    }
}