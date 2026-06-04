package com.strange.safety.emergency.dto;

import com.strange.safety.emergency.entity.EmergencyContact;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EmergencyContactResponse {

    private Long emergencyContactId;
    private Long protectedTargetId;
    private Long guardianUserId;
    private String name;
    private String relationship;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EmergencyContactResponse from(EmergencyContact contact) {
        return EmergencyContactResponse.builder()
                .emergencyContactId(contact.getId())
                .protectedTargetId(contact.getProtectedTarget().getId())
                .guardianUserId(contact.getGuardianUser().getId())
                .name(contact.getName())
                .relationship(contact.getRelationship())
                .phoneNumber(contact.getPhoneNumber())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .build();
    }
}
