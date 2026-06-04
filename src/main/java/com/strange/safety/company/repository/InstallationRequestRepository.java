package com.strange.safety.company.repository;

import com.strange.safety.company.entity.InstallationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstallationRequestRepository extends JpaRepository<InstallationRequest, Long> {
}
