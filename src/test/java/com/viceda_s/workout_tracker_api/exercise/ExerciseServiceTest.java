package com.viceda_s.workout_tracker_api.exercise;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    // Test 1: Get By ID (Success)
    @Test
    void getExerciseById_Success() {
        Exercise dummyExercise = new Exercise();
        dummyExercise.setId(1L);
        dummyExercise.setName("Squat");

        when(exerciseRepository.findById(1L)).thenReturn(Optional.of(dummyExercise));
        Exercise result = exerciseService.getExerciseById(1L);
        assertNotNull(result);
        assertEquals("Squat", result.getName());
    }

    // Test 2: Get By ID (Not Found)
    @Test
    void getExerciseById_NotFound_ThrowsException() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> {
            exerciseService.getExerciseById(99L);
        });
    }
    
    // Test 3: Filter By Exercise Type
    @Test
    void getExercisesByType_CallsRepository() {
        exerciseService.getExercisesByType(ExerciseType.CARDIO);
        verify(exerciseRepository).findByType(ExerciseType.CARDIO);
    }

    // Test 4: Filter By Muscle Group
    @Test
    void getExerciseByMuscleGroup_CallsRepository() {
        exerciseService.getExerciseByMuscleGroup(MuscleGroup.CORE);
        verify(exerciseRepository).findByMuscleGroup(MuscleGroup.CORE);
    }
}
