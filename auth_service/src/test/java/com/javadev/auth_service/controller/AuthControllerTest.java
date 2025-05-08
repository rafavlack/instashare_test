package com.javadev.auth_service.controller;

import com.javadev.auth_service.dto.AuthResponse;
import com.javadev.auth_service.dto.LoginRequest;
import com.javadev.auth_service.dto.RefreshTokenRequest;
import com.javadev.auth_service.dto.RegisterRequest;
import com.javadev.auth_service.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void whenRegister_thenReturnAuthResponse() {
        // Arrange
        RegisterRequest request = new RegisterRequest("user", "password", "user@example.com");
        AuthResponse expectedResponse = new AuthResponse("accessToken", "refreshToken");

        when(authService.register(request)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.register(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(authService).register(request);
    }

    @Test
    void whenLogin_thenReturnAuthResponse() {
        // Arrange
        LoginRequest request = new LoginRequest("user", "password");
        AuthResponse expectedResponse = new AuthResponse("accessToken", "refreshToken");

        when(authService.login(request)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.login(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(authService).login(request);
    }

    @Test
    void whenRefreshToken_thenReturnNewAuthResponse() {
        // Arrange
        String refreshToken = "refreshToken";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        AuthResponse expectedResponse = new AuthResponse("newAccessToken", "newRefreshToken");

        when(authService.refresh(refreshToken)).thenReturn(expectedResponse);

        // Act
        ResponseEntity<AuthResponse> response = authController.refresh(request);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(authService).refresh(refreshToken);
    }
}