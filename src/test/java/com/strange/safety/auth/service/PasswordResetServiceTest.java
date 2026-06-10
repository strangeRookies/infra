package com.strange.safety.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.strange.safety.auth.dto.PasswordResetRequest;
import com.strange.safety.auth.dto.PasswordResetSmsRequest;
import com.strange.safety.auth.dto.SmsVerificationRequest;
import com.strange.safety.auth.dto.SmsVerificationResponse;
import com.strange.safety.auth.entity.RefreshToken;
import com.strange.safety.auth.entity.Role;
import com.strange.safety.auth.entity.VerificationPurpose;
import com.strange.safety.auth.repository.RefreshTokenRepository;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.entity.UserStatus;
import com.strange.safety.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private SmsVerificationService smsVerificationService;

    private PasswordEncoder passwordEncoder;
    private PasswordResetService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new PasswordResetService(userRepository, refreshTokenRepository, passwordEncoder, smsVerificationService);
    }

    @Test
    void sendSmsIssuesResetPasswordVerificationWhenEmailAndPhoneMatch() {
        User user = user("user@example.com", "01012345678", "old-password-hash");
        when(smsVerificationService.normalizePhone("010-1234-5678")).thenReturn("01012345678");
        when(userRepository.findByEmailAndPhoneNumberAndStatus(
                "user@example.com", "01012345678", UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(smsVerificationService.send(any(SmsVerificationRequest.class)))
                .thenReturn(new SmsVerificationResponse(1L, 300));

        SmsVerificationResponse response = service.sendSms(
                new PasswordResetSmsRequest("USER@example.com", "010-1234-5678"));

        ArgumentCaptor<SmsVerificationRequest> captor = ArgumentCaptor.forClass(SmsVerificationRequest.class);
        verify(smsVerificationService).send(captor.capture());
        assertThat(response.verificationId()).isEqualTo(1L);
        assertThat(captor.getValue().phone()).isEqualTo("01012345678");
        assertThat(captor.getValue().purpose()).isEqualTo(VerificationPurpose.RESET_PASSWORD);
    }

    @Test
    void sendSmsFailsWhenUserDoesNotMatchEmailAndPhone() {
        when(smsVerificationService.normalizePhone("01012345678")).thenReturn("01012345678");
        when(userRepository.findByEmailAndPhoneNumberAndStatus(
                "missing@example.com", "01012345678", UserStatus.ACTIVE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendSms(
                new PasswordResetSmsRequest("missing@example.com", "01012345678")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void resetPasswordChangesPasswordAndRevokesRefreshTokens() {
        User user = user("user@example.com", "01012345678", passwordEncoder.encode("OldPassword123!"));
        RefreshToken firstRefreshToken = RefreshToken.issue(user, "hash-1", Instant.now().plusSeconds(3600));
        RefreshToken secondRefreshToken = RefreshToken.issue(user, "hash-2", Instant.now().plusSeconds(3600));
        when(smsVerificationService.normalizePhone("01012345678")).thenReturn("01012345678");
        when(userRepository.findByEmailAndPhoneNumberAndStatus(
                "user@example.com", "01012345678", UserStatus.ACTIVE)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findAllByUserId(1L)).thenReturn(List.of(firstRefreshToken, secondRefreshToken));

        service.resetPassword(new PasswordResetRequest(
                "user@example.com", "01012345678", "verified-token", "NewPassword123!"));

        verify(smsVerificationService).consume(
                "verified-token", "01012345678", VerificationPurpose.RESET_PASSWORD);
        assertThat(passwordEncoder.matches("NewPassword123!", user.getPasswordHash())).isTrue();
        assertThat(passwordEncoder.matches("OldPassword123!", user.getPasswordHash())).isFalse();
        assertThat(firstRefreshToken.isRevoked()).isTrue();
        assertThat(secondRefreshToken.isRevoked()).isTrue();
    }

    private User user(String email, String phone, String passwordHash) {
        User user = User.create(email, passwordHash, "사용자", phone, Role.INDIVIDUAL);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
