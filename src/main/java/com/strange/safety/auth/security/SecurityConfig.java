package com.strange.safety.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers(
                                "/api/auth/signup/individual",
                                "/api/auth/signup/corporate",
                                "/api/auth/login",
                                "/api/auth/reissue",
                                "/api/auth/verifications/sms",
                                "/api/auth/verifications/sms/confirm",
                                "/api/auth/email-availability",
                                "/api/companies/business-number-availability"
                        ).permitAll()

                        // USER 접근 가능 — /api/facilities/** ADMIN 규칙보다 반드시 먼저 선언
                        .requestMatchers(
                                "/api/facilities/*/alert-events/**",
                                "/api/facilities/*/protected-targets/**"
                        ).hasAnyRole("ADMIN", "INDIVIDUAL", "CORPORATE")
                        .requestMatchers(
                                "/api/alert-events/**",
                                "/api/protected-targets/**",
                                "/api/emergency-contacts/**"
                        ).hasAnyRole("ADMIN", "INDIVIDUAL", "CORPORATE")

                        // ADMIN 전용
                        .requestMatchers(
                                "/api/facilities/**",
                                "/api/cameras/**",
                                "/api/roi-configs/**",
                                "/api/scenarios/**"
                        ).hasRole("ADMIN")

                        // 문의(Inquiry) 접근 제어
                        .requestMatchers(HttpMethod.POST, "/api/inquiries").hasAnyRole("INDIVIDUAL", "CORPORATE")
                        .requestMatchers("/api/inquiries/my").hasAnyRole("INDIVIDUAL", "CORPORATE")
                        .requestMatchers(HttpMethod.GET, "/api/inquiries/{inquiryId}").hasAnyRole("ADMIN", "INDIVIDUAL", "CORPORATE")
                        .requestMatchers(HttpMethod.GET, "/api/inquiries").hasRole("ADMIN")
                        .requestMatchers("/api/inquiries/*/answer").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
