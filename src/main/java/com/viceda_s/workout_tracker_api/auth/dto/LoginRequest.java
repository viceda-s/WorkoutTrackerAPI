package com.viceda_s.workout_tracker_api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Email
    @Schema(example="vince@example.com")
    private String email;

    @NotBlank
    @Schema(example="SecurePass123")
    private String password;
}
