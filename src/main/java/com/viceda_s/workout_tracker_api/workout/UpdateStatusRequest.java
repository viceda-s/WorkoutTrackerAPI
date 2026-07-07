package com.viceda_s.workout_tracker_api.workout;

import lombok.Data;

@Data
public class UpdateStatusRequest {
    private WorkoutStatus status;    
}
