package com.strange.safety.user.dto;

public record AgreementRequest(
        Boolean termsAgreed,
        Boolean privacyAgreed,
        Boolean marketingAgreed
) {
    public boolean isMarketingAgreed() {
        return Boolean.TRUE.equals(marketingAgreed);
    }
}
