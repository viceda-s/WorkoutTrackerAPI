package com.viceda_s.workout_tracker_api.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleDataIntegrityViolationException_WithContraintName() {
        ConstraintViolationException cve = new ConstraintViolationException("error", new SQLException(),
                "a_custom_constraint");
        DataIntegrityViolationException dive = new DataIntegrityViolationException("wrapper", cve);

        ProblemDetail pd = handler.handleDataIntegrityViolationException(dive);

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(pd.getDetail()).isEqualTo("Resource conflict: Data already in use");
    }
}
