package com.javadev.auth_service.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class LoginRequest {
    private String email;
    private String password;
}
