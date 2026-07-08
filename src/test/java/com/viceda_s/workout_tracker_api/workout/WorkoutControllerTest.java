package com.viceda_s.workout_tracker_api.workout;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viceda_s.workout_tracker_api.auth.CustomUserDetailsService;
import com.viceda_s.workout_tracker_api.auth.JwtService;
import com.viceda_s.workout_tracker_api.config.SecurityConfig;
import com.viceda_s.workout_tracker_api.workout.dto.CreateWorkoutRequest;

@WebMvcTest(WorkoutController.class)
@Import(SecurityConfig.class)
public class WorkoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WorkoutService workoutService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private void stubValidToken(String token, String email) {
        UserDetails userDetails = User.builder()
                .username(email)
                .password("hashed")
                .authorities(Collections.emptyList())
                .build();
        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
    }

    private WorkoutPlan buildWorkoutPlan() {
        WorkoutPlan plan = new WorkoutPlan();
        plan.setId(1L);
        plan.setName("Push Day");
        plan.setScheduledAt(Instant.parse("2027-01-01T10:00:00Z"));
        plan.setStatus(WorkoutStatus.PLANNED);
        plan.setExercises(List.of());
        return plan;
    }

    @Test
    void createWorkout_NoToken_Returns401() throws Exception {
        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Push Day");
        request.setScheduledAt(Instant.now());
        request.setExercises(List.of());

        mockMvc.perform(post("/api/workouts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createWorkout_ValidToken_ReturnsCreatedWorkout() throws Exception {
        stubValidToken("validtoken", "vince@example.com");

        CreateWorkoutRequest.ExerciseLine line = new CreateWorkoutRequest.ExerciseLine();
        line.setExerciseId(1L);
        line.setSets(3);
        line.setReps(10);
        line.setWeight(new BigDecimal("50.0"));

        CreateWorkoutRequest request = new CreateWorkoutRequest();
        request.setName("Push Day");
        request.setScheduledAt(Instant.parse("2027-01-01T10:00:00Z"));
        request.setExercises(List.of(line));

        when(workoutService.createWorkout(eq("vince@example.com"), any())).thenReturn(buildWorkoutPlan());

        mockMvc.perform(post("/api/workouts")
                        .header("Authorization", "Bearer validtoken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Push Day"))
                .andExpect(jsonPath("$.status").value("PLANNED"));

        verify(workoutService).createWorkout(eq("vince@example.com"), any());
    }

    @Test
    void listWorkouts_ValidToken_ReturnsWorkouts() throws Exception {
        stubValidToken("validtoken", "vince@example.com");
        when(workoutService.listWorkouts("vince@example.com", null)).thenReturn(List.of(buildWorkoutPlan()));

        mockMvc.perform(get("/api/workouts").header("Authorization", "Bearer validtoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Push Day"));
    }

    @Test
    void listWorkouts_NoToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/workouts"))
                .andExpect(status().isUnauthorized());
    }
}
