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

/**
 * Unit tests for {@link ExerciseService}, covering lookup by id (success and
 * not-found cases) and filtering by exercise type and muscle group.
 */
@ExtendWith(MockitoExtension.class)
public class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @InjectMocks
    private ExerciseService exerciseService;

    /**
     * Fetching an exercise by a valid id should return that exercise.
     */
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

    /**
     * Fetching an exercise by an id that doesn't exist should throw a
     * {@link ResponseStatusException} rather than returning null.
     */
    @Test
    void getExerciseById_NotFound_ThrowsException() {
        when(exerciseRepository.findById(99L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            exerciseService.getExerciseById(99L);
        });
        assertNotNull(exception);
    }
    
    /**
     * Filtering by exercise type should delegate to the repository's
     * findByType query with the given type.
     */
    @Test
    void getExercisesByType_CallsRepository() {
        exerciseService.getExercisesByType(ExerciseType.CARDIO);
        verify(exerciseRepository).findByType(ExerciseType.CARDIO);
    }

    /**
     * Filtering by muscle group should delegate to the repository's
     * findByMuscleGroup query with the given muscle group.
     */
    @Test
    void getExerciseByMuscleGroup_CallsRepository() {
        exerciseService.getExerciseByMuscleGroup(MuscleGroup.CORE);
        verify(exerciseRepository).findByMuscleGroup(MuscleGroup.CORE);
    }
}
