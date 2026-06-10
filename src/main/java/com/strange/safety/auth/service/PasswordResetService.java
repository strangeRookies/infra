package com.strange.safety.auth.service;

import com.strange.safety.auth.dto.PasswordResetRequest;
import com.strange.safety.auth.dto.PasswordResetSmsRequest;
import com.strange.safety.auth.dto.SmsVerificationRequest;
import com.strange.safety.auth.dto.SmsVerificationResponse;
import com.strange.safety.auth.entity.RefreshToken;
import com.strange.safety.auth.entity.VerificationPurpose;
import com.strange.safety.auth.repository.RefreshTokenRepository;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.entity.UserStatus;
import com.strange.safety.user.repository.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsVerificationService smsVerificationService;

    public SmsVerificationResponse sendSms(PasswordResetSmsRequest request) {
        String email = normalizeEmail(request.email());
        String phone = smsVerificationService.normalizePhone(request.phone());
        findActiveUser(email, phone);
        return smsVerificationService.send(new SmsVerificationRequest(phone, VerificationPurpose.RESET_PASSWORD));
    }

    public void resetPassword(PasswordResetRequest request) {
        String email = normalizeEmail(request.email());
        String phone = smsVerificationService.normalizePhone(request.phone());
        User user = findActiveUser(email, phone);
        smsVerificationService.consume(request.verificationToken(), phone, VerificationPurpose.RESET_PASSWORD);
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        refreshTokenRepository.findAllByUserId(user.getId()).forEach(RefreshToken::revoke);
    }

    private User findActiveUser(String email, String phone) {
        return userRepository.findByEmailAndPhoneNumberAndStatus(email, phone, UserStatus.ACTIVE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
