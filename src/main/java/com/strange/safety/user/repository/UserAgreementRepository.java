package com.strange.safety.user.repository;

import com.strange.safety.user.entity.AgreementType;
import com.strange.safety.user.entity.UserAgreement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    List<UserAgreement> findByUserIdOrderByAgreementTypeAsc(Long userId);

    Optional<UserAgreement> findByUserIdAndAgreementType(Long userId, AgreementType agreementType);
}
