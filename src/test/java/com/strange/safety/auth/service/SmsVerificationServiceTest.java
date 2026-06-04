package com.strange.safety.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.strange.safety.auth.dto.SmsVerificationConfirmRequest;
import com.strange.safety.auth.dto.SmsVerificationRequest;
import com.strange.safety.auth.dto.SmsVerificationResponse;
import com.strange.safety.auth.entity.SmsVerification;
import com.strange.safety.auth.entity.VerificationPurpose;
import com.strange.safety.auth.repository.SmsVerificationRepository;
import com.strange.safety.auth.security.RefreshTokenHasher;
import com.strange.safety.common.exception.CustomException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmsVerificationServiceTest {

    @Mock
    private SmsVerificationRepository repository;
    @Mock
    private RefreshTokenHasher tokenHasher;

    private SmsVerificationService service;

    @BeforeEach
    void setUp() {
        service = new SmsVerificationService(repository, new BCryptPasswordEncoder(), tokenHasher);
        ReflectionTestUtils.setField(service, "localVerificationCode", "123456");
    }

    @Test
    void sendAndConfirmIssuesSingleUseVerificationToken() {
        when(repository.save(any(SmsVerification.class))).thenAnswer(invocation -> {
            SmsVerification verification = invocation.getArgument(0);
            ReflectionTestUtils.setField(verification, "id", 1L);
            return verification;
        });
        SmsVerificationResponse issued = service.send(
                new SmsVerificationRequest("010-1234-5678", VerificationPurpose.SIGN_UP));
        SmsVerification verification = repository.save(SmsVerification.issue(
                "01012345678", VerificationPurpose.SIGN_UP,
                new BCryptPasswordEncoder().encode("123456"), java.time.Instant.now().plusSeconds(300)));
        when(repository.findById(issued.verificationId())).thenReturn(Optional.of(verification));
        when(tokenHasher.hash(any(String.class))).thenReturn("token-hash");
        when(repository.findByVerificationTokenHash("token-hash")).thenReturn(Optional.of(verification));

        var confirmed = service.confirm(new SmsVerificationConfirmRequest(issued.verificationId(), "123456"));
        service.consume(confirmed.verificationToken(), "01012345678", VerificationPurpose.SIGN_UP);

        assertThat(confirmed.verified()).isTrue();
        assertThatThrownBy(() -> service.consume(
                confirmed.verificationToken(), "01012345678", VerificationPurpose.SIGN_UP))
                .isInstanceOf(CustomException.class);
    }
}
