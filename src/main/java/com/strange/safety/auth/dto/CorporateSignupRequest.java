package com.strange.safety.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CorporateSignupRequest(
        @Email @NotBlank String email,
        @Size(min = 8, max = 100) @NotBlank String password,
        @NotBlank String phone,
        @NotBlank String verificationToken,
        @Valid @NotNull CompanyRequest company,
        @Valid @NotNull ManagerRequest manager,
        @Valid @NotNull InstallationRequest installation
) {
    public record CompanyRequest(
            @NotBlank String name,
            @NotBlank String businessNumber,
            @NotBlank String industry,
            @NotBlank String size,
            @NotBlank String postcode,
            @NotBlank String address,
            String addressDetail,
            String district,
            String jurisdiction
    ) {
    }

    public record ManagerRequest(
            @NotBlank String name,
            String department,
            String rank,
            @Email @NotBlank String email,
            @NotBlank String contact
    ) {
    }

    public record InstallationRequest(
            @NotBlank String count,
            @NotNull LocalDate preferredDate,
            String specialRequest
    ) {
    }
}
