package com.viceda_s.workout_tracker_api.workout;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    @Schema(example = "COMPLETED")
    private WorkoutStatus status;
}
