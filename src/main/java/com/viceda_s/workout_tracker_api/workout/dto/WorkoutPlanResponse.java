package com.viceda_s.workout_tracker_api.workout.dto;

import java.time.Instant;
import java.util.List;

import com.viceda_s.workout_tracker_api.workout.WorkoutStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkoutPlanResponse {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "Push Day")
    private String name;

    @Schema(example = "2026-07-10T09:00:00Z")
    private Instant scheduledAt;

    @Schema(example = "PLANNED")
    private WorkoutStatus status;

    private List<WorkoutExerciseResponse> exercises;
}

