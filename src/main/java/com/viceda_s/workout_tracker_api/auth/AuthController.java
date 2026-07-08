package com.viceda_s.workout_tracker_api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.viceda_s.workout_tracker_api.auth.dto.AuthResponse;
import com.viceda_s.workout_tracker_api.auth.dto.LoginRequest;
import com.viceda_s.workout_tracker_api.auth.dto.RegisterRequest;
import com.viceda_s.workout_tracker_api.auth.dto.RegisterResponse;
import com.viceda_s.workout_tracker_api.user.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name="Auth", description="Registration and login")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Register a new user",
            description = "Creates a new account with a BCrypt-hashed password. Returns 409 if the email is already registered.")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "409", description = "Email already in use")
    @ApiResponse(responseCode = "400", description = "Validation failed (blank fields, invalid email)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        User savedUser = authService.register(request);
        return new RegisterResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getName());
    }
    
    @Operation(summary = "Log in",
            description = "Validates credentials and returns a signed JWT. Returns 401 for either a wrong password or an unknown email, without revealing which one was wrong.")
    @ApiResponse(responseCode = "200", description = "Login successful, JWT returned")
    @ApiResponse(responseCode = "401", description = "Invalid email or password")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public String me(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getUsername();
    }
}
