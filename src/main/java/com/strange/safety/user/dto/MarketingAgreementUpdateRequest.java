package com.strange.safety.user.dto;

import jakarta.validation.constraints.NotNull;

public record MarketingAgreementUpdateRequest(
        @NotNull Boolean agreed
) {
}
