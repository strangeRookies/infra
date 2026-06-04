package com.strange.safety.auth.dto;

import com.strange.safety.auth.entity.Role;
import com.strange.safety.user.entity.User;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresIn,
        LoginUserResponse user
) {

    public static TokenResponse bearer(String accessToken, String refreshToken,
                                       long accessTokenExpiresInMs, User user) {
        return new TokenResponse(
                "Bearer",
                accessToken,
                refreshToken,
                accessTokenExpiresInMs / 1000,
                LoginUserResponse.from(user)
        );
    }

    public record LoginUserResponse(Long id, String email, String name, Role role) {
        public static LoginUserResponse from(User user) {
            return new LoginUserResponse(user.getId(), user.getEmail(), user.getName(), user.getRole());
        }
    }
}
