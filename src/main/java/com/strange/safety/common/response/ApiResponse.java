package com.strange.safety.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ErrorDetail error
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "요청에 성공했습니다.", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static ApiResponse<Void> error(String message) {
        return error(null, message);
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, null, new ErrorDetail(code, message, null));
    }

    public static ApiResponse<Void> validationError(String code, String message, Map<String, String> fieldErrors) {
        return new ApiResponse<>(false, null, null, new ErrorDetail(code, message, fieldErrors));
    }

    public record ErrorDetail(String code, String message, Map<String, String> fieldErrors) {
    }
}
