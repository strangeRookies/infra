package com.strange.safety.auth.dto;

import com.strange.safety.auth.entity.VerificationPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SmsVerificationRequest(
        @NotBlank(message = "휴대폰 번호는 필수입니다.") String phone,
        @NotNull(message = "인증 목적은 필수입니다.") VerificationPurpose purpose
) {
}
