package com.javadev.auth_service.service;

import com.javadev.auth_service.dto.AuthResponse;
import com.javadev.auth_service.dto.LoginRequest;
import com.javadev.auth_service.dto.RefreshToken;
import com.javadev.auth_service.dto.RegisterRequest;
import com.javadev.auth_service.entity.User;
import com.javadev.auth_service.repository.RefreshTokenRepository;
import com.javadev.auth_service.repository.UserRepository;
import com.javadev.auth_service.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ya registrado");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        userRepository.save(user);

        return generateTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Password inválida");
        }

        return generateTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .filter(rt -> !rt.isRevoked())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido o revocado"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expirado");
        }

        User user = token.getUser();
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        return generateTokens(user);
    }

    private AuthResponse generateTokens(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        RefreshToken entity = RefreshToken.builder()
                .token(refreshToken)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .user(user)
                .revoked(false)
                .build();

        refreshTokenRepository.save(entity);
        return new AuthResponse(accessToken, refreshToken);
    }
}
