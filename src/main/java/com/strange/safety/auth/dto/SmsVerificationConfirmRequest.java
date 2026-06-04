package com.strange.safety.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SmsVerificationConfirmRequest(
        @NotNull(message = "verificationId는 필수입니다.") Long verificationId,
        @NotBlank(message = "인증번호는 필수입니다.") String code
) {
}
