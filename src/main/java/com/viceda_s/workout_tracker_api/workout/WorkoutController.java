package com.viceda_s.workout_tracker_api.workout;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutPlan createWorkout(@Valid @RequestBody CreateWorkoutRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.createWorkout(userDetails.getUsername(), request);
    }

    @GetMapping
    public List<WorkoutPlan> listWorkouts(@RequestParam(required=false) WorkoutStatus status, @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.listWorkouts(userDetails.getUsername(), status);
    }

    @GetMapping("/{id}")
    public WorkoutPlan getWorkoutById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.getWorkoutById(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    public WorkoutPlan updateWorkout(@PathVariable Long id, @Valid @RequestBody CreateWorkoutRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.updateWorkout(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkout(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        workoutService.deleteWorkout(userDetails.getUsername(), id);
    }

    @PatchMapping("/{id}/status")
    public WorkoutPlan updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.updateStatus(userDetails.getUsername(), id, request.getStatus());
    }
}
