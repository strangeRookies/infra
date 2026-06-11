package com.strange.safety.facility.dto;

import com.strange.safety.facility.entity.Facility;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminFacilityResponse {

    private Long facilityId;
    private String facilityName;
    private String companyName;

    public static AdminFacilityResponse from(Facility facility) {
        String companyName = facility.getCompanyProfile() != null
                ? facility.getCompanyProfile().getCompanyName()
                : null;
        return AdminFacilityResponse.builder()
                .facilityId(facility.getId())
                .facilityName(facility.getFacilityName())
                .companyName(companyName)
                .build();
    }
}
