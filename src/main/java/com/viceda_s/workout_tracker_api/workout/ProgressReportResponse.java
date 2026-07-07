package com.viceda_s.workout_tracker_api.workout;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Value;

@Value
public class ProgressReportResponse {
    @Schema(example = "4")
    long totalCompletedWorkouts;
    List<ExerciseVolumeSummary> exerciseVolumes;
}
