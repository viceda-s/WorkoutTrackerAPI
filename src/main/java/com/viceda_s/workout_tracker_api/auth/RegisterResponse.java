package com.viceda_s.workout_tracker_api.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {

    @Schema(example="1")
    private Long id;

    @Schema(example="tua_prima@example.com")
    private String email;

    @Schema(example="Tua Prima")
    private String name;
}
