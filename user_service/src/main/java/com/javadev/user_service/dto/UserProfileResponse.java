package com.javadev.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {
    private String fullName;
    private String email;
    private String avatar;

    public UserProfileResponse(String fullName, String email, String avatar) {
        this.fullName = fullName;
        this.email = email;
        this.avatar = avatar;
    }
}
