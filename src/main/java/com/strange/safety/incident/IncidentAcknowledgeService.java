package com.strange.safety.incident;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class IncidentAcknowledgeService {

    private final IncidentRecordingRepository repository;
    private final Clock clock;

    public IncidentAcknowledgeService(IncidentRecordingRepository repository) {
        this(repository, Clock.systemUTC());
    }

    IncidentAcknowledgeService(IncidentRecordingRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public IncidentRecordingRecord acknowledgeAndRequestRecording(
            String pathEventId,
            AcknowledgeIncidentRequest request
    ) {
        String eventId = request.eventId().isBlank() ? pathEventId : request.eventId();
        IncidentRecordingRecord record = new IncidentRecordingRecord(
                eventId,
                request.cameraId(),
                request.eventType(),
                request.eventTimestamp(),
                Instant.now(clock),
                request.acknowledgedBy(),
                request.preFrames(),
                request.postFrames(),
                request.totalFrames(),
                RecordingStatus.RECORDING_REQUESTED
        );
        return repository.save(record);
    }
}
