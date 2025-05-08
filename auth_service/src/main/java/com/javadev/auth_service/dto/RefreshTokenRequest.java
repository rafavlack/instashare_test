package com.javadev.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token no puede estar vacío")
    private String refreshToken;
}
