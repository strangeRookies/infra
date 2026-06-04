package com.strange.safety.auth.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record IndividualSignupRequest(
        @Email @NotBlank String email,
        @Size(min = 8, max = 100) @NotBlank String password,
        @NotBlank String name,
        @NotBlank String phone,
        @NotBlank String verificationToken,
        @Valid @NotNull CareTargetRequest careTarget,
        @Valid List<EmergencyContactRequest> emergencyContacts
) {
    public record CareTargetRequest(
            @NotBlank String name,
            @NotBlank String relation,
            @NotBlank String ageGroup,
            @NotBlank String postcode,
            @NotBlank String address,
            String addressDetail,
            String district,
            String jurisdiction
    ) {
    }

    public record EmergencyContactRequest(
            @NotBlank String name,
            @NotBlank String relation,
            @NotBlank String phone
    ) {
    }
}
