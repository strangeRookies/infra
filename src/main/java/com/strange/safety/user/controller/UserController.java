package com.strange.safety.user.controller;

import com.strange.safety.auth.security.AuthenticatedUser;
import com.strange.safety.common.response.ApiResponse;
import com.strange.safety.user.dto.UserResponse;
import com.strange.safety.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.success(userService.getMe(user.getUserId()));
    }
}
