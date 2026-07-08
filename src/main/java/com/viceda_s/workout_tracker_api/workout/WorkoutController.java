package com.viceda_s.workout_tracker_api.workout;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.viceda_s.workout_tracker_api.workout.dto.CreateWorkoutRequest;
import com.viceda_s.workout_tracker_api.workout.dto.ProgressReportResponse;
import com.viceda_s.workout_tracker_api.workout.dto.UpdateStatusRequest;
import com.viceda_s.workout_tracker_api.workout.dto.WorkoutExerciseResponse;
import com.viceda_s.workout_tracker_api.workout.dto.WorkoutPlanResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Workouts", description = "Create, manage, and report on workout plans (all endpoints require authentication)")
@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    private WorkoutExerciseResponse toResponse(WorkoutExercise we) {
        return new WorkoutExerciseResponse(
                we.getExercise().getId(),
                we.getExercise().getName(),
                we.getSets(),
                we.getReps(),
                we.getWeight(),
                we.getOrderIndex()
        );
    }

    private WorkoutPlanResponse toResponse(WorkoutPlan plan) {
        List<WorkoutExerciseResponse> exercises = plan.getExercises().stream()
                .map(this::toResponse)
                .toList();
        return new WorkoutPlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getScheduledAt(),
                plan.getStatus(),
                exercises
        );
    }

    @Operation(summary = "Create a workout plan",
            description = "Builds a new plan from a list of exercises with sets/reps/weight. Always starts in PLANNED status.")
    @ApiResponse(responseCode = "201", description = "Workout plan created")
    @ApiResponse(responseCode = "400", description = "An exerciseId in the request doesn't exist")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutPlanResponse createWorkout(@Valid @RequestBody CreateWorkoutRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return toResponse(workoutService.createWorkout(userDetails.getUsername(), request));
    }

    @Operation(summary = "List your workout plans",
            description = "Returns only the caller's own plans, sorted by scheduledAt ascending, optionally filtered by status.")
    @ApiResponse(responseCode = "200", description = "Workout plans returned")
    @GetMapping
    public List<WorkoutPlanResponse> listWorkouts(
            @Parameter(description = "Filter by status", example = "PLANNED")
            @RequestParam(required=false) WorkoutStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.listWorkouts(userDetails.getUsername(), status)
                .stream().map(this::toResponse).toList();
    }

    @Operation(summary = "Get a single workout plan",
            description = "Returns 404 if the plan doesn't exist, or belongs to someone else — the two cases are indistinguishable.")
    @ApiResponse(responseCode = "200", description = "Workout plan found")
    @ApiResponse(responseCode = "404", description = "No such plan, or it isn't yours")
    @GetMapping("/{id}")
    public WorkoutPlanResponse getWorkoutById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        return toResponse(workoutService.getWorkoutById(userDetails.getUsername(), id));
    }

    @Operation(summary = "Update a workout plan",
            description = "Replaces the plan's name, scheduledAt, and exercises entirely — the exercises list is not merged with the existing one.")
    @ApiResponse(responseCode = "200", description = "Workout plan updated")
    @ApiResponse(responseCode = "404", description = "No such plan, or it isn't yours")
    @ApiResponse(responseCode = "400", description = "An exerciseId in the request doesn't exist")
    @PutMapping("/{id}")
    public WorkoutPlanResponse updateWorkout(@PathVariable Long id, @Valid @RequestBody CreateWorkoutRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return toResponse(workoutService.updateWorkout(userDetails.getUsername(), id, request));
    }

    @Operation(summary = "Delete a workout plan", description = "Also deletes all of its associated exercises.")
    @ApiResponse(responseCode = "204", description = "Workout plan deleted")
    @ApiResponse(responseCode = "404", description = "No such plan, or it isn't yours")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkout(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        workoutService.deleteWorkout(userDetails.getUsername(), id);
    }

    @Operation(summary = "Change a workout plan's status", description = "Transitions between PLANNED, COMPLETED, and CANCELED.")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "404", description = "No such plan, or it isn't yours")
    @PatchMapping("/{id}/status")
    public WorkoutPlanResponse updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return toResponse(workoutService.updateStatus(userDetails.getUsername(), id, request.getStatus()));
    }

    @Operation(summary = "Get a progress report",
            description = "Totals completed workouts and per-exercise volume (sets × reps × weight) within a date range. PLANNED and CANCELED plans are excluded.")
    @ApiResponse(responseCode = "200", description = "Progress report generated")
    @GetMapping("/reports")
    public ProgressReportResponse getProgressReport(
            @Parameter(description = "Start of the date range (inclusive)", example = "2026-07-01T00:00:00Z")
            @RequestParam Instant from,
            @Parameter(description = "End of the date range (inclusive)", example = "2026-07-31T23:59:59Z")
            @RequestParam Instant to,
            @AuthenticationPrincipal UserDetails userDetails) {
        return workoutService.generateProgressReport(userDetails.getUsername(), from, to);
    }
}
