package com.strange.safety.auth.service;

import com.strange.safety.auth.dto.*;
import com.strange.safety.auth.entity.SmsVerification;
import com.strange.safety.auth.entity.VerificationPurpose;
import com.strange.safety.auth.repository.SmsVerificationRepository;
import com.strange.safety.auth.security.RefreshTokenHasher;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SmsVerificationService {

    private static final long CODE_EXPIRATION_SECONDS = 300;
    private static final long TOKEN_EXPIRATION_SECONDS = 900;

    private final SmsVerificationRepository smsVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenHasher tokenHasher;

    @Value("${sms.local-verification-code:123456}")
    private String localVerificationCode;

    public SmsVerificationResponse send(SmsVerificationRequest request) {
        SmsVerification verification = SmsVerification.issue(
                normalizePhone(request.phone()), request.purpose(),
                passwordEncoder.encode(localVerificationCode),
                Instant.now().plusSeconds(CODE_EXPIRATION_SECONDS)
        );
        SmsVerification saved = smsVerificationRepository.save(verification);
        return new SmsVerificationResponse(saved.getId(), CODE_EXPIRATION_SECONDS);
    }

    @Transactional(noRollbackFor = CustomException.class)
    public SmsVerificationConfirmResponse confirm(SmsVerificationConfirmRequest request) {
        SmsVerification verification = smsVerificationRepository.findById(request.verificationId())
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_VERIFICATION));
        Instant now = Instant.now();
        if (!verification.canConfirm(now)) {
            throw new CustomException(ErrorCode.AUTH_INVALID_VERIFICATION);
        }
        if (!passwordEncoder.matches(request.code(), verification.getCodeHash())) {
            verification.recordFailure();
            throw new CustomException(ErrorCode.AUTH_INVALID_VERIFICATION);
        }
        String token = UUID.randomUUID().toString();
        verification.verify(tokenHasher.hash(token), now.plusSeconds(TOKEN_EXPIRATION_SECONDS), now);
        return new SmsVerificationConfirmResponse(true, token);
    }

    public void consume(String token, String phone, VerificationPurpose purpose) {
        SmsVerification verification = smsVerificationRepository
                .findByVerificationTokenHash(tokenHasher.hash(token))
                .orElseThrow(() -> new CustomException(ErrorCode.AUTH_INVALID_VERIFICATION));
        Instant now = Instant.now();
        if (!verification.canUse(normalizePhone(phone), purpose, now)) {
            throw new CustomException(ErrorCode.AUTH_INVALID_VERIFICATION);
        }
        verification.use(now);
    }

    public String normalizePhone(String phone) {
        return phone == null ? null : phone.replaceAll("[^0-9]", "");
    }
}
