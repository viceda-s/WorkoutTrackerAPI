package com.viceda_s.workout_tracker_api.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank
    @Email
    @Schema(example="tua_prima@example.com")
    private String email;

    @NotBlank
    @Schema(example="aTuaPrimaDe4")
    private String password;

    @NotBlank
    @Schema(example="Tua Prima")
    private String name;
}
