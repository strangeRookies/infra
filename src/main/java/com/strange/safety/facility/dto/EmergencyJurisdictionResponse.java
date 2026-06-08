package com.strange.safety.facility.dto;

public record EmergencyJurisdictionResponse(
        String district,
        String jurisdiction,
        String stationName,
        String centerName,
        String stationAddress,
        Double latitude,
        Double longitude
) {
}
