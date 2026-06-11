package com.strange.safety.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetSmsConfirmRequest(
        @Email @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String code
) {
}
