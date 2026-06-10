package com.strange.safety.event;

import com.strange.safety.alert.service.AlertEventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEventProcessorService {

    private static final Logger log = LoggerFactory.getLogger(AsyncEventProcessorService.class);

    private final AlertEventService alertEventService;
    private final AlertBroadcastService alertBroadcastService;

    @Async("eventProcessingExecutor")
    public void processEvent(SafetyEventDto event) {
        try {
            alertEventService.createEvent(event);
            alertBroadcastService.broadcast(event);
        } catch (RuntimeException ex) {
            log.error("Failed to process safety event asynchronously: cameraId={}, type={}, error={}",
                    event.cameraId(), event.type(), ex.getMessage(), ex);
        }
    }
}
