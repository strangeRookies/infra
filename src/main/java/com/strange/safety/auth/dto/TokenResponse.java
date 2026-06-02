package com.strange.safety.auth.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInMs,
        long refreshTokenExpiresInMs
) {

    public static TokenResponse bearer(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresInMs,
            long refreshTokenExpiresInMs
    ) {
        return new TokenResponse(
                "Bearer",
                accessToken,
                refreshToken,
                accessTokenExpiresInMs,
                refreshTokenExpiresInMs
        );
    }
}
