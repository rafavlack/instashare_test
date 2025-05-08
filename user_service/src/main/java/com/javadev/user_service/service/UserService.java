package com.javadev.user_service.service;

import com.javadev.user_service.dto.UserProfileRequest;
import com.javadev.user_service.dto.UserProfileResponse;
import com.javadev.user_service.entity.User;
import com.javadev.user_service.repository.UserRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MinioClient minioClient;

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    public UserProfileResponse getUserProfile() {
        User user = getAuthenticatedUser();
        return new UserProfileResponse(user.getFullName(), user.getEmail(), user.getAvatar());
    }

    @Transactional
    public void updateUserProfile(UserProfileRequest profileRequest) {
        User user = getAuthenticatedUser();
        user.setFullName(profileRequest.getFullName());
        user.setBio(profileRequest.getBio());
        userRepository.save(user);
    }

    @Transactional
    public void uploadAvatar(MultipartFile avatarFile) {
        try {
            String objectName = UUID.randomUUID() + "_" + avatarFile.getOriginalFilename();
            InputStream inputStream = avatarFile.getInputStream();

            String bucketName = "avatars";
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, avatarFile.getSize(), -1)
                            .contentType(avatarFile.getContentType())
                            .build()
            );

            User user = getAuthenticatedUser();
            user.setAvatar(objectName);
            userRepository.save(user);

        } catch (Exception e) {
            throw new RuntimeException("Error al subir el avatar", e);
        }
    }
}
