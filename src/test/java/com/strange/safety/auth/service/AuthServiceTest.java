package com.strange.safety.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.strange.safety.auth.dto.LoginRequest;
import com.strange.safety.auth.dto.LogoutRequest;
import com.strange.safety.auth.dto.SignupRequest;
import com.strange.safety.auth.dto.TokenReissueRequest;
import com.strange.safety.auth.dto.TokenResponse;
import com.strange.safety.auth.entity.RefreshToken;
import com.strange.safety.auth.repository.RefreshTokenRepository;
import com.strange.safety.auth.security.JwtTokenProvider;
import com.strange.safety.auth.security.RefreshTokenHasher;
import com.strange.safety.common.exception.CustomException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.user.dto.UserResponse;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.repository.UserRepository;
import java.time.Instant;
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
class AuthServiceTest {

    private static final String RAW_PASSWORD = "password123";
    private static final String REFRESH_TOKEN = "refresh-token";
    private static final String REFRESH_TOKEN_HASH = "refresh-token-hash";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenHasher refreshTokenHasher;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtTokenProvider,
                refreshTokenHasher
        );
    }

    @Test
    void signupCreatesUserWithEncodedPassword() {
        SignupRequest request = new SignupRequest("TEST@EXAMPLE.COM", RAW_PASSWORD, "홍길동", "010-1234-5678");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return user;
        });

        UserResponse response = authService.signup(request);

        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("test@example.com");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(passwordEncoder.matches(RAW_PASSWORD, userCaptor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void signupFailsWhenEmailAlreadyExists() {
        SignupRequest request = new SignupRequest("test@example.com", RAW_PASSWORD, "홍길동", null);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
    }

    @Test
    void loginIssuesTokens() {
        User user = user("test@example.com", passwordEncoder.encode(RAW_PASSWORD));
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken()).thenReturn(REFRESH_TOKEN);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(1800000L);
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(1209600000L);
        when(refreshTokenHasher.hash(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);

        TokenResponse response = authService.login(new LoginRequest("test@example.com", RAW_PASSWORD));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void loginFailsWithWrongPassword() {
        User user = user("test@example.com", passwordEncoder.encode(RAW_PASSWORD));
        when(userRepository.findByEmailAndIsActiveTrue("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("test@example.com", "wrong-password")))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_INVALID_PASSWORD);
    }

    @Test
    void reissueRotatesRefreshToken() {
        User user = user("test@example.com", passwordEncoder.encode(RAW_PASSWORD));
        RefreshToken savedRefreshToken = RefreshToken.issue(user, REFRESH_TOKEN_HASH, Instant.now().plusSeconds(60));
        when(refreshTokenHasher.hash(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(refreshTokenRepository.findByTokenHash(REFRESH_TOKEN_HASH)).thenReturn(Optional.of(savedRefreshToken));
        when(jwtTokenProvider.createAccessToken(user)).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken()).thenReturn("new-refresh-token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(1800000L);
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(1209600000L);
        when(refreshTokenHasher.hash("new-refresh-token")).thenReturn("new-refresh-token-hash");

        TokenResponse response = authService.reissue(new TokenReissueRequest(REFRESH_TOKEN));

        assertThat(savedRefreshToken.isRevoked()).isTrue();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void logoutRevokesRefreshTokenAndPreventsReuse() {
        User user = user("test@example.com", passwordEncoder.encode(RAW_PASSWORD));
        RefreshToken savedRefreshToken = RefreshToken.issue(user, REFRESH_TOKEN_HASH, Instant.now().plusSeconds(60));
        when(refreshTokenHasher.hash(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_HASH);
        when(refreshTokenRepository.findByTokenHash(REFRESH_TOKEN_HASH)).thenReturn(Optional.of(savedRefreshToken));

        authService.logout(new LogoutRequest(REFRESH_TOKEN));

        assertThat(savedRefreshToken.isRevoked()).isTrue();
        assertThatThrownBy(() -> authService.reissue(new TokenReissueRequest(REFRESH_TOKEN)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
    }

    private User user(String email, String passwordHash) {
        User user = User.create(email, passwordHash, "홍길동", null);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
