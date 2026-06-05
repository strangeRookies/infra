package com.strange.safety.inquiry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryCategory {
    CAMERA_VIDEO("카메라 및 영상"),
    ALERT_ALARM("알림 및 경보"),
    MOBILE("모바일"),
    OTHER("기타");

    private final String description;
}
