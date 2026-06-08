package com.strange.safety.user.dto;

import com.strange.safety.user.entity.AgreementType;
import com.strange.safety.user.entity.UserAgreement;
import java.time.LocalDateTime;

public record UserAgreementResponse(
        AgreementType agreementType,
        boolean required,
        boolean agreed,
        LocalDateTime agreedAt,
        LocalDateTime withdrawnAt
) {
    public static UserAgreementResponse from(UserAgreement agreement) {
        return new UserAgreementResponse(
                agreement.getAgreementType(),
                agreement.isRequired(),
                agreement.isAgreed(),
                agreement.getAgreedAt(),
                agreement.getWithdrawnAt()
        );
    }
}
