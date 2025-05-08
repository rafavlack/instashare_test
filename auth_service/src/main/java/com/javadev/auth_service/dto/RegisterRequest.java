package com.javadev.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class RegisterRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String fullName;
}
