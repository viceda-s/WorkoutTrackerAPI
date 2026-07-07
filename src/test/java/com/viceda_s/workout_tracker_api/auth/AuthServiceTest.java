package com.viceda_s.workout_tracker_api.auth;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.viceda_s.workout_tracker_api.user.User;
import com.viceda_s.workout_tracker_api.user.UserRepository;

/**
 * Unit tests for {@link AuthService}, covering successful registration and
 * rejection of duplicate emails.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    /**
     * Registering with an email that isn't already in use should save 
     * the user with their password hashed, not stored in plaintext.
     */ 
    @Test
    void registerNewEmail_SavesHashedPassword() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword123")).thenReturn("hashed-value");
    
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("plainPassword123");
        request.setName("Test");

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("hashed-value", savedUser.getPassword());
        assertNotEquals("plainPassword123", savedUser.getPassword());
    }

    /**
     * Registering with an email already in use should be rejected 
     * properly before any write on the database.
     */
    @Test
    void registerDupEmail() {
        
        User existingUser = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("whatever");
        request.setName("Test");
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            authService.register(request));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userRepository, never()).save(any());
    }
}
