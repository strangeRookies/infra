package com.strange.safety.camera.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BulkCameraUploadResult {

    private int successCount;
    private int failCount;
    private List<CameraResponse> registeredCameras;
    private List<FailedRow> failedRows;

    @Getter
    @AllArgsConstructor
    public static class FailedRow {
        private int rowNumber;
        private String reason;
    }
}
