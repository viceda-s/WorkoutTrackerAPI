package com.viceda_s.workout_tracker_api.workout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.viceda_s.workout_tracker_api.exercise.Exercise;
import com.viceda_s.workout_tracker_api.exercise.ExerciseRepository;
import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.user.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    
    private final WorkoutPlanRepository workoutPlanRepository;
    private final WorkoutExerciseRepository workoutExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserService userService;

    private List<WorkoutExercise> buildWorkoutExercises(WorkoutPlan plan, List<CreateWorkoutRequest.ExerciseLine> lines) {
        List<WorkoutExercise> exercises = new ArrayList<>();
        int orderIndex = 0;
        for (CreateWorkoutRequest.ExerciseLine line : lines) {
            Exercise exercise = exerciseRepository.findById(line.getExerciseId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown exercise id: " + line.getExerciseId()));

            WorkoutExercise we = new WorkoutExercise();
            we.setWorkoutPlan(plan);
            we.setExercise(exercise);
            we.setSets(line.getSets());
            we.setReps(line.getReps());
            we.setWeight(line.getWeight());
            we.setOrderIndex(orderIndex++);

            exercises.add(we);
        }
        return exercises;
    }

    public WorkoutPlan createWorkout(String ownerEmail, CreateWorkoutRequest request) {
        User owner = userService.getByEmailOrThrow(ownerEmail);
        
        WorkoutPlan plan = new WorkoutPlan();
        plan.setOwner(owner);
        plan.setName(request.getName());
        plan.setScheduledAt(request.getScheduledAt());
        plan.setStatus(WorkoutStatus.PLANNED);

        plan.setExercises(buildWorkoutExercises(plan, request.getExercises()));

        return workoutPlanRepository.save(plan);
    }

    public List<WorkoutPlan> listWorkouts(String ownerEmail, WorkoutStatus status) {
        User owner = userService.getByEmailOrThrow(ownerEmail);
        
        if (status == null) {
            return workoutPlanRepository.findByOwnerOrderByScheduledAtAsc(owner);
        } else {
            return workoutPlanRepository.findByOwnerAndStatusOrderByScheduledAtAsc(owner, status);
        }
    }

    private WorkoutPlan requireOwnedWorkout(String ownerEmail, Long id) {
        User owner = userService.getByEmailOrThrow(ownerEmail);
        return workoutPlanRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workout not found"));
    }

    public WorkoutPlan getWorkoutById(String ownerEmail, Long id) {
        return requireOwnedWorkout(ownerEmail, id);
    }

    public WorkoutPlan updateWorkout(String ownerEmail, Long id, CreateWorkoutRequest request) {
        WorkoutPlan plan = requireOwnedWorkout(ownerEmail, id);

        plan.setName(request.getName());
        plan.setScheduledAt(request.getScheduledAt());

        plan.getExercises().clear();
        plan.getExercises().addAll(buildWorkoutExercises(plan, request.getExercises()));

        return workoutPlanRepository.save(plan);
    }

    public void deleteWorkout(String ownerEmail, Long id) {
        WorkoutPlan plan = requireOwnedWorkout(ownerEmail, id);
        workoutPlanRepository.delete(plan);
    }

    public WorkoutPlan updateStatus(String ownerEmail, Long id, WorkoutStatus newStatus) {
        WorkoutPlan plan = requireOwnedWorkout(ownerEmail, id);
        plan.setStatus(newStatus);

        return workoutPlanRepository.save(plan);
    }

    public ProgressReportResponse generateProgressReport(String ownerEmail, Instant from, Instant to) {
        User owner = userService.getByEmailOrThrow(ownerEmail);

        long totalCompleted = workoutPlanRepository.countByOwnerAndStatusAndScheduledAtBetween(
                owner, WorkoutStatus.COMPLETED, from, to);
        List<ExerciseVolumeSummary> volumes = workoutExerciseRepository.summarizeVolumeByOwnerAndPeriod(
            owner, from, to);

        return new ProgressReportResponse(totalCompleted, volumes);
    }
}
