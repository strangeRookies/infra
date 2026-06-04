package com.strange.safety.incident;

import java.time.Instant;

public record IncidentRecordingRecord(
        String eventId,
        String cameraId,
        String eventType,
        Instant eventTimestamp,
        Instant acknowledgedAt,
        String acknowledgedBy,
        int preFrames,
        int postFrames,
        int totalFrames,
        RecordingStatus recordingStatus
) {
}
