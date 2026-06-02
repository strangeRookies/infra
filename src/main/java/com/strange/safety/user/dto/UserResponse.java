package com.strange.safety.user.dto;

import com.strange.safety.user.entity.User;
import com.strange.safety.user.entity.UserRole;

public record UserResponse(
        Long userId,
        String email,
        String name,
        String phoneNumber,
        boolean phoneVerified,
        UserRole role
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.isPhoneVerified(),
                user.getRole()
        );
    }
}
