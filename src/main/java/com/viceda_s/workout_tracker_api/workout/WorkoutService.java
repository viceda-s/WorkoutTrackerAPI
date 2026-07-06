package com.viceda_s.workout_tracker_api.workout;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.viceda_s.workout_tracker_api.exercise.Exercise;
import com.viceda_s.workout_tracker_api.exercise.ExerciseRepository;
import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkoutService {
    
    private final WorkoutPlanRepository workoutPlanRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public WorkoutPlan createWorkout(String ownerEmail, CreateWorkoutRequest request) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        WorkoutPlan plan = new WorkoutPlan();
        plan.setOwner(owner);
        plan.setName(request.getName());
        plan.setScheduledAt(request.getScheduledAt());
        plan.setStatus(WorkoutStatus.PLANNED);

        List<WorkoutExercise> exercises = new ArrayList<>();
        int orderIndex = 0;
        for (CreateWorkoutRequest.ExerciseLine line :request.getExercises()) {
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
        plan.setExercises(exercises);
        return workoutPlanRepository.save(plan);
    }

    public List<WorkoutPlan> listWorkouts(String ownerEmail, WorkoutStatus status) {
        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        
        if (status == null) {
            return workoutPlanRepository.findByOwnerOrderByScheduledAtAsc(owner);
        } else {
            return workoutPlanRepository.findByOwnerAndStatusOrderByScheduledAtAsc(owner, status);
        }
    }
}
