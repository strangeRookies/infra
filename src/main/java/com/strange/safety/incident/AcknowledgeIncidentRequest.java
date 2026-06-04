package com.strange.safety.incident;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record AcknowledgeIncidentRequest(
        @NotBlank
        String eventId,

        @NotBlank
        String cameraId,

        @NotBlank
        String eventType,

        @NotNull
        Instant eventTimestamp,

        @Min(0)
        int preFrames,

        @Min(0)
        int postFrames,

        @Min(1)
        int totalFrames,

        @NotBlank
        String status,

        @NotBlank
        String reason,

        String acknowledgedBy
) {
}
