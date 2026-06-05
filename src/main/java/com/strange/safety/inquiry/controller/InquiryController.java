package com.strange.safety.inquiry.controller;

import com.strange.safety.auth.security.CustomUserDetails;
import com.strange.safety.common.response.ApiResponse;
import com.strange.safety.inquiry.dto.InquiryAnswerRequest;
import com.strange.safety.inquiry.dto.InquiryCreateRequest;
import com.strange.safety.inquiry.dto.InquiryResponse;
import com.strange.safety.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ApiResponse<Long> createInquiry(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InquiryCreateRequest request) {
        return ApiResponse.success(inquiryService.createInquiry(userDetails.getUserId(), request));
    }

    @GetMapping("/my")
    public ApiResponse<List<InquiryResponse>> getMyInquiries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(inquiryService.getMyInquiries(userDetails.getUserId()));
    }

    @GetMapping("/{inquiryId}")
    public ApiResponse<InquiryResponse> getInquiry(@PathVariable Long inquiryId) {
        return ApiResponse.success(inquiryService.getInquiry(inquiryId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<InquiryResponse>> getAllInquiries() {
        return ApiResponse.success(inquiryService.getAllInquiries());
    }

    @PostMapping("/{inquiryId}/answer")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> answerInquiry(
            @PathVariable Long inquiryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody InquiryAnswerRequest request) {
        inquiryService.answerInquiry(inquiryId, userDetails.getUserId(), request);
        return ApiResponse.success(null);
    }
}
