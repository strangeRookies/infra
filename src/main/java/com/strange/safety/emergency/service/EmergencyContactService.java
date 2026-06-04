package com.strange.safety.emergency.service;

import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.emergency.dto.CreateEmergencyContactRequest;
import com.strange.safety.emergency.dto.EmergencyContactResponse;
import com.strange.safety.emergency.dto.UpdateEmergencyContactRequest;
import com.strange.safety.emergency.entity.EmergencyContact;
import com.strange.safety.emergency.repository.EmergencyContactRepository;
import com.strange.safety.facility.entity.ProtectedTarget;
import com.strange.safety.facility.repository.ProtectedTargetRepository;
import com.strange.safety.facility.service.FacilityService;
import com.strange.safety.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final ProtectedTargetRepository protectedTargetRepository;
    private final FacilityService facilityService;
    private final UserRepository userRepository;

    @Transactional
    public EmergencyContactResponse create(Long userId, Long targetId, CreateEmergencyContactRequest request) {
        ProtectedTarget target = getTargetWithOwnerCheck(userId, targetId);

        EmergencyContact contact = EmergencyContact.builder()
                .guardianUser(userRepository.getReferenceById(userId))
                .protectedTarget(target)
                .name(request.getName())
                .relationship(request.getRelationship())
                .phoneNumber(request.getPhoneNumber())
                .build();

        return EmergencyContactResponse.from(emergencyContactRepository.save(contact));
    }

    public List<EmergencyContactResponse> getList(Long userId, Long targetId) {
        getTargetWithOwnerCheck(userId, targetId);
        return emergencyContactRepository.findByProtectedTarget_Id(targetId).stream()
                .map(EmergencyContactResponse::from)
                .collect(Collectors.toList());
    }

    public EmergencyContactResponse getOne(Long userId, Long contactId) {
        EmergencyContact contact = getContactWithOwnerCheck(userId, contactId);
        return EmergencyContactResponse.from(contact);
    }

    @Transactional
    public EmergencyContactResponse update(Long userId, Long contactId, UpdateEmergencyContactRequest request) {
        EmergencyContact contact = getContactWithOwnerCheck(userId, contactId);
        contact.update(request.getName(), request.getRelationship(), request.getPhoneNumber());
        return EmergencyContactResponse.from(contact);
    }

    @Transactional
    public void delete(Long userId, Long contactId) {
        EmergencyContact contact = getContactWithOwnerCheck(userId, contactId);
        emergencyContactRepository.delete(contact);
    }

    private ProtectedTarget getTargetWithOwnerCheck(Long userId, Long targetId) {
        ProtectedTarget target = protectedTargetRepository.findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROTECTED_TARGET_NOT_FOUND));
        facilityService.getFacilityWithOwnerCheck(userId, target.getFacility().getId());
        return target;
    }

    private EmergencyContact getContactWithOwnerCheck(Long userId, Long contactId) {
        EmergencyContact contact = emergencyContactRepository.findById(contactId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_CONTACT_NOT_FOUND));
        facilityService.getFacilityWithOwnerCheck(userId,
                contact.getProtectedTarget().getFacility().getId());
        return contact;
    }
}
