package com.strange.safety.facility.controller;

import com.strange.safety.common.response.ApiResponse;
import com.strange.safety.facility.dto.EmergencyJurisdictionResolveRequest;
import com.strange.safety.facility.dto.EmergencyJurisdictionResponse;
import com.strange.safety.facility.service.EmergencyJurisdictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emergency-jurisdictions")
@RequiredArgsConstructor
public class EmergencyJurisdictionController {

    private final EmergencyJurisdictionService emergencyJurisdictionService;

    @PostMapping("/resolve")
    public ApiResponse<EmergencyJurisdictionResponse> resolve(
            @Valid @RequestBody EmergencyJurisdictionResolveRequest request
    ) {
        return ApiResponse.success(emergencyJurisdictionService.resolve(request));
    }
}
