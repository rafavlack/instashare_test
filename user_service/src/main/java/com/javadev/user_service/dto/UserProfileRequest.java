package com.javadev.user_service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileRequest {
    private String fullName;
    private String bio;
}
