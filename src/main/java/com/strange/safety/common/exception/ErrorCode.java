package com.strange.safety.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    COMMON_INVALID_INPUT("COMMON_INVALID_INPUT", "요청 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    COMMON_INTERNAL_ERROR("COMMON_INTERNAL_ERROR", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    AUTH_INVALID_PASSWORD("AUTH_INVALID_PASSWORD", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID_TOKEN("AUTH_INVALID_TOKEN", "인증 토큰이 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_EXPIRED_TOKEN("AUTH_EXPIRED_TOKEN", "인증 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    AUTH_UNAUTHORIZED("AUTH_UNAUTHORIZED", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    AUTH_FORBIDDEN("AUTH_FORBIDDEN", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_EMAIL_ALREADY_EXISTS("USER_EMAIL_ALREADY_EXISTS", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
