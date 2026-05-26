package com.strange.safety.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class AlertBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(AlertBroadcastService.class);
    private static final String ALERT_TOPIC = "/topic/alerts";

    private final SimpMessagingTemplate messagingTemplate;

    public AlertBroadcastService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(SafetyEventDto event) {
        log.info("Broadcasting safety event to {}: type={}, cameraId={}, severity={}",
                ALERT_TOPIC, event.type(), event.cameraId(), event.severity());
        messagingTemplate.convertAndSend(ALERT_TOPIC, event);
    }
}
