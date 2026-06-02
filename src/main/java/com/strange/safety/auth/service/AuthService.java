package com.strange.safety.auth.service;

import com.strange.safety.auth.dto.LoginRequest;
import com.strange.safety.auth.dto.LogoutRequest;
import com.strange.safety.auth.dto.SignupRequest;
import com.strange.safety.auth.dto.TokenReissueRequest;
import com.strange.safety.auth.dto.TokenResponse;
import com.strange.safety.auth.entity.RefreshToken;
import com.strange.safety.auth.repository.RefreshTokenRepository;
import com.strange.safety.auth.security.JwtTokenProvider;
import com.strange.safety.auth.security.RefreshTokenHasher;
import com.strange.safety.common.exception.BusinessException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.user.dto.UserResponse;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.repository.UserRepository;
import java.time.Instant;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenHasher refreshTokenHasher;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenHasher refreshTokenHasher
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenHasher = refreshTokenHasher;
    }

    public UserResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        User user = User.create(
                email,
                passwordEncoder.encode(request.password()),
                request.name().trim(),
                normalizePhoneNumber(request.phoneNumber())
        );
        return UserResponse.from(userRepository.save(user));
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(normalizeEmail(request.email()))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_PASSWORD));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_INVALID_PASSWORD);
        }

        return issueTokens(user);
    }

    public TokenResponse reissue(TokenReissueRequest request) {
        RefreshToken refreshToken = findRefreshToken(request.refreshToken());
        Instant now = Instant.now();
        if (refreshToken.isRevoked() || refreshToken.isExpired(now)) {
            refreshToken.revoke();
            throw new BusinessException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        refreshToken.revoke();
        return issueTokens(refreshToken.getUser());
    }

    public void logout(LogoutRequest request) {
        RefreshToken refreshToken = findRefreshToken(request.refreshToken());
        refreshToken.revoke();
    }

    private TokenResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken();
        Instant refreshTokenExpiresAt = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());
        refreshTokenRepository.save(RefreshToken.issue(
                user,
                refreshTokenHasher.hash(refreshToken),
                refreshTokenExpiresAt
        ));

        return TokenResponse.bearer(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationMs(),
                jwtTokenProvider.getRefreshTokenExpirationMs()
        );
    }

    private RefreshToken findRefreshToken(String token) {
        return refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(token))
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_INVALID_TOKEN));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            return null;
        }
        return phoneNumber.trim();
    }
}
