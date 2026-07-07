package com.viceda_s.workout_tracker_api.workout;

import java.util.List;

import lombok.Value;

@Value
public class ProgressReportResponse {
    long totalCompletedWorkouts;
    List<ExerciseVolumeSummary> exerciseVolumes;
}
