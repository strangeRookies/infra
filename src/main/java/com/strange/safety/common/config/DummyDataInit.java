package com.strange.safety.common.config;

import com.strange.safety.auth.entity.Role;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DummyDataInit {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String testEmail = "test@example.com";
            // DB에 해당 이메일이 없을 때만 생성 (중복 방지)
            if (userRepository.findByEmail(testEmail).isEmpty()) {
                User dummyUser = User.builder()
                        .email(testEmail)
                        .password(passwordEncoder.encode("123456")) // 비밀번호 암호화
                        .name("테스트유저")
                        .phoneNumber("010-1234-5678")
                        .role(Role.USER)
                        .build();
                
                userRepository.save(dummyUser);
                System.out.println("✅ 테스트용 임시 유저가 생성되었습니다: " + testEmail + " / 123456");
            }
        };
    }
}
