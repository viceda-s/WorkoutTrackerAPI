package com.viceda_s.workout_tracker_api.workout;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.viceda_s.workout_tracker_api.user.User;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {
    List<WorkoutPlan> findByOwnerOrderByScheduledAtAsc(User owner);
    List<WorkoutPlan> findByOwnerAndStatusOrderByScheduledAtAsc(User owner, WorkoutStatus status);
    Optional<WorkoutPlan> findByIdAndOwner(Long id, User owner);
}
