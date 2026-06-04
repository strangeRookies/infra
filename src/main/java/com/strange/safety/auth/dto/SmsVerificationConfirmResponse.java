package com.strange.safety.auth.dto;

public record SmsVerificationConfirmResponse(boolean verified, String verificationToken) {
}
