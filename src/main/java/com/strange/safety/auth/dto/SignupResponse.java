package com.strange.safety.auth.dto;

import com.strange.safety.auth.entity.Role;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.entity.UserStatus;
import java.time.LocalDateTime;

public record SignupResponse(
        Long userId,
        String email,
        Role role,
        UserStatus status,
        LocalDateTime createdAt
) {
    public static SignupResponse from(User user) {
        return new SignupResponse(
                user.getId(), user.getEmail(), user.getRole(), user.getStatus(), user.getCreatedAt()
        );
    }
}
