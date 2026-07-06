package com.viceda_s.workout_tracker_api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.JwtException;

public class JwtServiceTest {
    private final JwtService jwtService = new JwtService("some-32+-char-test-secret-value-here");

    @Test
    void generateToken_ThenExtractEmail_RoundTrips() {
        String token = jwtService.generateToken("test@example.com");
        String extracted = jwtService.extractEmail(token);
        assertEquals("test@example.com", extracted);
    }

    @Test
    void extractEmail_GarbageToken_ThrowsJwtException() {
        assertThrows(JwtException.class, () -> jwtService.extractEmail("not-a-real-token"));
    }
}
