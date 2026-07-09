package com.viceda_s.workout_tracker_api.workout;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.viceda_s.workout_tracker_api.user.User;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    @EntityGraph(attributePaths = { "exercises", "exercises.exercise" })
    List<WorkoutPlan> findByOwnerOrderByScheduledAtAsc(User owner);

    @EntityGraph(attributePaths = { "exercises", "exercises.exercise" })
    List<WorkoutPlan> findByOwnerAndStatusOrderByScheduledAtAsc(User owner, WorkoutStatus status);

    @EntityGraph(attributePaths = { "exercises", "exercises.exercise" })
    Optional<WorkoutPlan> findByIdAndOwner(Long id, User owner);

    long countByOwnerAndStatusAndScheduledAtBetween(User owner, WorkoutStatus status, Instant from, Instant to);
}
