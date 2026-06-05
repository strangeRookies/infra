package com.strange.safety.emergency.controller;

import com.strange.safety.auth.security.CustomUserDetails;
import com.strange.safety.common.response.ApiResponse;
import com.strange.safety.emergency.dto.CreateEmergencyContactRequest;
import com.strange.safety.emergency.dto.EmergencyContactResponse;
import com.strange.safety.emergency.dto.UpdateEmergencyContactRequest;
import com.strange.safety.emergency.service.EmergencyContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService emergencyContactService;

    @PostMapping("/api/protected-targets/{targetId}/emergency-contacts")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetId,
            @Valid @RequestBody CreateEmergencyContactRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        emergencyContactService.create(userDetails.getUserId(), targetId, request)));
    }

    @GetMapping("/api/protected-targets/{targetId}/emergency-contacts")
    public ResponseEntity<ApiResponse<List<EmergencyContactResponse>>> getList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetId) {
        return ResponseEntity.ok(ApiResponse.success(
                emergencyContactService.getList(userDetails.getUserId(), targetId)));
    }

    @GetMapping("/api/emergency-contacts/{contactId}")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> getOne(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contactId) {
        return ResponseEntity.ok(ApiResponse.success(
                emergencyContactService.getOne(userDetails.getUserId(), contactId)));
    }

    @PutMapping("/api/emergency-contacts/{contactId}")
    public ResponseEntity<ApiResponse<EmergencyContactResponse>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contactId,
            @Valid @RequestBody UpdateEmergencyContactRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                emergencyContactService.update(userDetails.getUserId(), contactId, request)));
    }

    @DeleteMapping("/api/emergency-contacts/{contactId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long contactId) {
        emergencyContactService.delete(userDetails.getUserId(), contactId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
