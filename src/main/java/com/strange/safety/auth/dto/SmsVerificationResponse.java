package com.strange.safety.auth.dto;

public record SmsVerificationResponse(Long verificationId, long expiresIn) {
}
