package com.strange.safety.incident;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class IncidentRecordingRepository {

    private final ConcurrentMap<String, IncidentRecordingRecord> records = new ConcurrentHashMap<>();

    public IncidentRecordingRecord save(IncidentRecordingRecord record) {
        records.put(record.eventId(), record);
        return record;
    }

    public Optional<IncidentRecordingRecord> findByEventId(String eventId) {
        return Optional.ofNullable(records.get(eventId));
    }
}
