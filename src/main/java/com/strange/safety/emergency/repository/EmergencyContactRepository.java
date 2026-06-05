package com.strange.safety.emergency.repository;

import com.strange.safety.emergency.entity.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByProtectedTarget_Id(Long protectedTargetId);
}
