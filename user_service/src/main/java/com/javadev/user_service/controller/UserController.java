package com.javadev.user_service.controller;

import com.javadev.user_service.dto.UserProfileRequest;
import com.javadev.user_service.dto.UserProfileResponse;
import com.javadev.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Ver perfil de usuario
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        UserProfileResponse profile = userService.getUserProfile();
        return ResponseEntity.ok(profile);
    }

    // Actualizar perfil de usuario
    @PutMapping("/profile")
    public ResponseEntity<Void> updateUserProfile(@RequestBody UserProfileRequest profileRequest) {
        userService.updateUserProfile(profileRequest);
        return ResponseEntity.ok().build();
    }

    // Subir avatar
    @PostMapping("/avatar")
    public ResponseEntity<Void> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        userService.uploadAvatar(avatar);
        return ResponseEntity.ok().build();
    }
}
