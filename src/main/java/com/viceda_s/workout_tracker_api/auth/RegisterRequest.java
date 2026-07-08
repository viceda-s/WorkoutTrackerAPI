package com.viceda_s.workout_tracker_api.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank
    @Email
    @Schema(example="vince@example.com")
    private String email;

    @NotBlank
    @Size(min=8, message="Password must be at least 8 characters")
    @Schema(example="SecurePass123")
    private String password;

    @NotBlank
    @Schema(example="Vince")
    private String name;
}
