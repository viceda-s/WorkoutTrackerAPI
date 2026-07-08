package com.viceda_s.workout_tracker_api.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viceda_s.workout_tracker_api.auth.dto.AuthResponse;
import com.viceda_s.workout_tracker_api.auth.dto.LoginRequest;
import com.viceda_s.workout_tracker_api.auth.dto.RegisterRequest;
import com.viceda_s.workout_tracker_api.config.SecurityConfig;
import com.viceda_s.workout_tracker_api.user.User;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    void register_ValidRequest_ReturnsCreated() throws Exception {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("vince@example.com");
        savedUser.setName("Vince");

        when(authService.register(any())).thenReturn(savedUser);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("vince@example.com");
        request.setPassword("longenough123");
        request.setName("Vince");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("vince@example.com"))
                .andExpect(jsonPath("$.name").value("Vince"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void register_DuplicateEmail_ReturnsConflict() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use"));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("vince@example.com");
        request.setPassword("longenough123");
        request.setName("Vince");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already in use"));
    }

    @Test
    void login_ValidCredentials_ReturnsToken() throws Exception {
        when(authService.login(any())).thenReturn(new AuthResponse("sometoken"));

        LoginRequest request = new LoginRequest();
        request.setEmail("vince@example.com");
        request.setPassword("longenough123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("sometoken"));
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        LoginRequest request = new LoginRequest();
        request.setEmail("vince@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_NoToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_ValidToken_ReturnsEmail() throws Exception {
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("vince@example.com")
                .password("hashed")
                .authorities(Collections.emptyList())
                .build();
        when(jwtService.extractEmail("validtoken")).thenReturn("vince@example.com");
        when(userDetailsService.loadUserByUsername("vince@example.com")).thenReturn(userDetails);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer validtoken"))
                .andExpect(status().isOk())
                .andExpect(content().string("vince@example.com"));
    }
}
