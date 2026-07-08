package com.viceda_s.workout_tracker_api.workout;

import java.time.Instant;
import java.util.List;

import com.viceda_s.workout_tracker_api.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="workout_plans")
@Getter
@Setter
@NoArgsConstructor
public class WorkoutPlan {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User owner;

    private String name;

    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    private WorkoutStatus status;

    @OneToMany(mappedBy = "workoutPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkoutExercise> exercises;
}
