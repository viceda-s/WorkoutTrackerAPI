package com.viceda_s.workout_tracker_api.workout;

import java.math.BigDecimal;

import lombok.Value;

@Value
public class ExerciseVolumeSummary {
    String exerciseName;
    BigDecimal totalVolume;

}
