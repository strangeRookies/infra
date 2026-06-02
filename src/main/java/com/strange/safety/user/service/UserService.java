package com.strange.safety.user.service;

import com.strange.safety.common.exception.BusinessException;
import com.strange.safety.common.exception.ErrorCode;
import com.strange.safety.user.dto.UserResponse;
import com.strange.safety.user.entity.User;
import com.strange.safety.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getMe(Long userId) {
        User user = userRepository.findByUserIdAndActiveTrue(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }
}
