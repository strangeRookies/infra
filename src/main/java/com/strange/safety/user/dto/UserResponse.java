package com.strange.safety.user.dto;

import com.strange.safety.auth.entity.Role;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.entity.UserStatus;

public record UserResponse(
        Long userId,
        String email,
        String name,
        String phoneNumber,
        boolean phoneVerified,
        Role role,
        UserStatus status
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.isPhoneVerified(),
                user.getRole(),
                user.getStatus()
        );
    }
}
