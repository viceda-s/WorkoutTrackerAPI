package com.viceda_s.workout_tracker_api.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

/**
 * Unit tests for {@link JwtService}, covering token generation, extraction,
 * and rejection of invalid tokens.
 */
public class JwtServiceTest {
    private final JwtService jwtService = new JwtService("some-32+-char-test-secret-value-here");
   
    /**
     * A token generated for a given email should, when parsed back,
     * yield that same email as its subject.
     */
    @Test
    void generateToken_ThenExtractEmail_RoundTrips() {
        String token = jwtService.generateToken("test@example.com");
        String extracted = jwtService.extractEmail(token);
        assertEquals("test@example.com", extracted);
    }

    /**
     * A string that isn't a validly-signed JWT at all (not just expired
     * or wrong-signature, but structurally invalid) must be rejected
     * rather than silently accepted.
     */
    @Test
    void extractEmail_GarbageToken_ThrowsJwtException() {
        JwtException thrown = assertThrows(JwtException.class, () -> jwtService.extractEmail("not-a-real-token"));
        thrown.getMessage();
    }
}
