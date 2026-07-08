package com.viceda_s.workout_tracker_api.exercise;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.viceda_s.workout_tracker_api.auth.CustomUserDetailsService;
import com.viceda_s.workout_tracker_api.auth.JwtService;

@WebMvcTest(controllers = ExerciseController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
public class ExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExerciseService exerciseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private Exercise buildExercise() {
        Exercise exercise = new Exercise();
        exercise.setId(1L);
        exercise.setName("Bench Press");
        exercise.setDescription("A compound chest exercise");
        exercise.setType(ExerciseType.STRENGTH);
        exercise.setMuscleGroup(MuscleGroup.CHEST);
        return exercise;
    }

    @Test
    void getExercises_ReturnsMappedResponse() throws Exception {
        when(exerciseService.getAllExercises()).thenReturn(List.of(buildExercise()));

        mockMvc.perform(get("/api/exercises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Bench Press"))
                .andExpect(jsonPath("$[0].type").value("STRENGTH"));
    }

    @Test
    void getExerciseById_Found_ReturnsExercise() throws Exception {
        when(exerciseService.getExerciseById(1L)).thenReturn(buildExercise());

        mockMvc.perform(get("/api/exercises/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bench Press"));
    }
}
