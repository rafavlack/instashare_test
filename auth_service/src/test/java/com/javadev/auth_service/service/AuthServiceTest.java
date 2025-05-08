package com.javadev.auth_service.service;

import com.javadev.auth_service.dto.AuthResponse;
import com.javadev.auth_service.dto.LoginRequest;
import com.javadev.auth_service.dto.RefreshToken;
import com.javadev.auth_service.dto.RegisterRequest;
import com.javadev.auth_service.entity.User;
import com.javadev.auth_service.repository.RefreshTokenRepository;
import com.javadev.auth_service.repository.UserRepository;
import com.javadev.auth_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldCreateUserAndReturnTokens() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "John Doe");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void register_shouldThrowIfEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "John");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email ya registrado");
    }

    @Test
    void login_shouldReturnTokensForValidCredentials() {
        User user = User.builder()
                .email("test@example.com")
                .password("hashed")
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtUtil.generateAccessToken(user)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("refresh-token");

        LoginRequest request = new LoginRequest("test@example.com", "password");

        AuthResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void login_shouldThrowIfUserNotFound() {
        when(userRepository.findByEmail("noone@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("noone@example.com", "pass");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Email no encontrado");
    }

    @Test
    void login_shouldThrowIfPasswordIncorrect() {
        User user = User.builder().email("test@example.com").password("hashed").build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest("test@example.com", "wrong");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Password inválida");
    }

    @Test
    void refresh_shouldReturnNewTokens() {
        User user = User.builder().email("test@example.com").build();
        RefreshToken token = RefreshToken.builder()
                .token("refresh-token")
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .user(user)
                .build();

        when(refreshTokenRepository.findByToken("refresh-token"))
                .thenReturn(Optional.of(token));
        when(jwtUtil.generateAccessToken(user)).thenReturn("new-access");
        when(jwtUtil.generateRefreshToken(user)).thenReturn("new-refresh");

        AuthResponse response = authService.refresh("refresh-token");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void refresh_shouldThrowIfTokenRevokedOrNotFound() {
        when(refreshTokenRepository.findByToken("bad-token"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("bad-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token inválido");
    }

    @Test
    void refresh_shouldThrowIfTokenExpired() {
        User user = User.builder().build();
        RefreshToken token = RefreshToken.builder()
                .token("expired")
                .revoked(false)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .user(user)
                .build();

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refresh("expired"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Refresh token expirado");
    }
}
