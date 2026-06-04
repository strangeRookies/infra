package com.strange.safety.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.strange.safety.auth.dto.CorporateSignupRequest;
import com.strange.safety.auth.dto.IndividualSignupRequest;
import com.strange.safety.auth.entity.Role;
import com.strange.safety.auth.entity.SmsVerification;
import com.strange.safety.auth.entity.VerificationPurpose;
import com.strange.safety.auth.repository.SmsVerificationRepository;
import com.strange.safety.auth.security.RefreshTokenHasher;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.company.repository.CompanyProfileRepository;
import com.strange.safety.company.repository.InstallationRequestRepository;
import com.strange.safety.emergency.repository.EmergencyContactRepository;
import com.strange.safety.event.MqttSafetyEventSubscriber;
import com.strange.safety.facility.repository.FacilityRepository;
import com.strange.safety.facility.repository.ProtectedTargetRepository;
import com.strange.safety.facility.repository.UserFacilityRepository;
import com.strange.safety.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
class SignupServiceIntegrationTest {

    @Autowired SignupService signupService;
    @Autowired UserRepository userRepository;
    @Autowired CompanyProfileRepository companyProfileRepository;
    @Autowired InstallationRequestRepository installationRequestRepository;
    @Autowired FacilityRepository facilityRepository;
    @Autowired UserFacilityRepository userFacilityRepository;
    @Autowired ProtectedTargetRepository protectedTargetRepository;
    @Autowired EmergencyContactRepository emergencyContactRepository;
    @Autowired SmsVerificationRepository smsVerificationRepository;
    @Autowired RefreshTokenHasher tokenHasher;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired PlatformTransactionManager transactionManager;

    @MockBean MqttSafetyEventSubscriber mqttSafetyEventSubscriber;

    @BeforeEach
    void cleanDatabase() {
        emergencyContactRepository.deleteAll();
        protectedTargetRepository.deleteAll();
        userFacilityRepository.deleteAll();
        facilityRepository.deleteAll();
        installationRequestRepository.deleteAll();
        companyProfileRepository.deleteAll();
        smsVerificationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void individualSignupCreatesAllRelatedData() {
        String token = verifiedToken("01011112222");

        var response = signupService.signupIndividual(individualRequest(
                "individual@example.com", "01011112222", token, "70대"));

        assertThat(response.role()).isEqualTo(Role.INDIVIDUAL);
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(facilityRepository.count()).isEqualTo(1);
        assertThat(userFacilityRepository.count()).isEqualTo(1);
        assertThat(protectedTargetRepository.count()).isEqualTo(1);
        assertThat(emergencyContactRepository.count()).isEqualTo(1);
    }

    @Test
    void individualSignupFailureRollsBackAllRelatedData() {
        String token = verifiedToken("01011112222");
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status ->
                signupService.signupIndividual(individualRequest(
                        "rollback@example.com", "01011112222", token, "UNKNOWN"))))
                .isInstanceOf(CustomException.class);

        assertThat(userRepository.count()).isZero();
        assertThat(facilityRepository.count()).isZero();
        assertThat(userFacilityRepository.count()).isZero();
        assertThat(protectedTargetRepository.count()).isZero();
        assertThat(emergencyContactRepository.count()).isZero();
    }

    @Test
    void corporateSignupCreatesProfileAndInstallationRequest() {
        String token = verifiedToken("01055556666");

        var response = signupService.signupCorporate(corporateRequest(
                "corporate@example.com", "01055556666", token, "123-45-67890"));

        assertThat(response.role()).isEqualTo(Role.CORPORATE);
        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(companyProfileRepository.count()).isEqualTo(1);
        assertThat(installationRequestRepository.count()).isEqualTo(1);
    }

    @Test
    void corporateSignupFailureRollsBackUserAndProfile() {
        String token = verifiedToken("01055556666");
        var invalid = new CorporateSignupRequest(
                "corporate-rollback@example.com", "Password123!", "01055556666", token,
                corporateRequest("x@example.com", "01055556666", token, "1234567890").company(),
                corporateRequest("x@example.com", "01055556666", token, "1234567890").manager(),
                new CorporateSignupRequest.InstallationRequest("6~15개소", null, null)
        );
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status ->
                signupService.signupCorporate(invalid)))
                .isInstanceOf(RuntimeException.class);

        assertThat(userRepository.count()).isZero();
        assertThat(companyProfileRepository.count()).isZero();
        assertThat(installationRequestRepository.count()).isZero();
    }

    @Test
    void duplicateEmailAndBusinessNumberAreRejected() {
        signupService.signupCorporate(corporateRequest(
                "duplicate@example.com", "01055556666",
                verifiedToken("01055556666"), "123-45-67890"));

        assertThatThrownBy(() -> signupService.signupIndividual(individualRequest(
                "DUPLICATE@example.com", "01011112222",
                verifiedToken("01011112222"), "70대")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_ALREADY_EXISTS);

        assertThatThrownBy(() -> signupService.signupCorporate(corporateRequest(
                "other@example.com", "01077778888",
                verifiedToken("01077778888"), "1234567890")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.COMPANY_BUSINESS_NUMBER_ALREADY_EXISTS);
    }

    private String verifiedToken(String phone) {
        String token = "verified-" + phone + "-" + System.nanoTime();
        SmsVerification verification = SmsVerification.issue(
                phone, VerificationPurpose.SIGN_UP,
                passwordEncoder.encode("123456"), Instant.now().plusSeconds(300));
        verification.verify(tokenHasher.hash(token), Instant.now().plusSeconds(900), Instant.now());
        smsVerificationRepository.save(verification);
        return token;
    }

    private IndividualSignupRequest individualRequest(String email, String phone, String token, String ageGroup) {
        return new IndividualSignupRequest(
                email, "Password123!", "개인 사용자", phone, token,
                new IndividualSignupRequest.CareTargetRequest(
                        "보호 대상자", "부모", ageGroup, "04123",
                        "서울특별시 마포구 월드컵로 1", "101동", "마포구", "마포소방서"),
                List.of(new IndividualSignupRequest.EmergencyContactRequest(
                        "비상 연락처", "자녀", "01033334444"))
        );
    }

    private CorporateSignupRequest corporateRequest(String email, String phone, String token, String businessNumber) {
        return new CorporateSignupRequest(
                email, "Password123!", phone, token,
                new CorporateSignupRequest.CompanyRequest(
                        "통합테스트기업", businessNumber, "의료/보건", "50~200인",
                        "06123", "서울특별시 강남구 테헤란로 1", "안전관리실", "강남구", "강남소방서"),
                new CorporateSignupRequest.ManagerRequest(
                        "기업 담당자", "안전관리팀", "과장", "manager@example.com", phone),
                new CorporateSignupRequest.InstallationRequest(
                        "6~15개소", LocalDate.of(2026, 7, 1), "실외 카메라 설치 필요")
        );
    }
}
