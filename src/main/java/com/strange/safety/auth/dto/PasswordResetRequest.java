package com.strange.safety.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @Email @NotBlank String email,
        @NotBlank String phone,
        @NotBlank String verificationToken,
        @Size(min = 8, max = 100) @NotBlank String newPassword
) {
}
