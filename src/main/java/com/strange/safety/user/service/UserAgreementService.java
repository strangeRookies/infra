package com.strange.safety.user.service;

import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.user.dto.AgreementRequest;
import com.strange.safety.user.dto.MarketingAgreementUpdateRequest;
import com.strange.safety.user.dto.UserAgreementResponse;
import com.strange.safety.user.entity.AgreementType;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.entity.UserAgreement;
import com.strange.safety.user.repository.UserAgreementRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAgreementService {

    private final UserAgreementRepository userAgreementRepository;

    public void validateRequiredAgreements(AgreementRequest agreements) {
        if (agreements == null
                || !Boolean.TRUE.equals(agreements.termsAgreed())
                || !Boolean.TRUE.equals(agreements.privacyAgreed())) {
            throw new CustomException(ErrorCode.AGREEMENT_REQUIRED);
        }
    }

    public void saveSignupAgreements(User user, AgreementRequest agreements) {
        validateRequiredAgreements(agreements);
        LocalDateTime now = LocalDateTime.now();
        userAgreementRepository.saveAll(List.of(
                UserAgreement.create(user, AgreementType.TERMS, true, true, now),
                UserAgreement.create(user, AgreementType.PRIVACY, true, true, now),
                UserAgreement.create(user, AgreementType.MARKETING, false, agreements.isMarketingAgreed(), now)
        ));
    }

    @Transactional(readOnly = true)
    public List<UserAgreementResponse> getMyAgreements(Long userId) {
        return userAgreementRepository.findByUserIdOrderByAgreementTypeAsc(userId)
                .stream()
                .map(UserAgreementResponse::from)
                .toList();
    }

    public UserAgreementResponse updateMarketingAgreement(Long userId, MarketingAgreementUpdateRequest request) {
        UserAgreement agreement = userAgreementRepository
                .findByUserIdAndAgreementType(userId, AgreementType.MARKETING)
                .orElseThrow(() -> new CustomException(ErrorCode.AGREEMENT_NOT_FOUND));
        agreement.updateAgreement(request.agreed(), LocalDateTime.now());
        return UserAgreementResponse.from(agreement);
    }
}
