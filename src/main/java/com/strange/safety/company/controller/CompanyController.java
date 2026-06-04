package com.strange.safety.company.controller;

import com.strange.safety.auth.dto.AvailabilityResponse;
import com.strange.safety.auth.service.SignupService;
import com.strange.safety.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final SignupService signupService;

    @GetMapping("/business-number-availability")
    public ApiResponse<AvailabilityResponse> businessNumberAvailability(@RequestParam String businessNumber) {
        return ApiResponse.success(signupService.businessNumberAvailability(businessNumber));
    }
}
