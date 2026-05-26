package com.strange.safety.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SafetyEventSubscriber implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(SafetyEventSubscriber.class);

    private final ObjectMapper objectMapper;
    private final AlertBroadcastService alertBroadcastService;

    public SafetyEventSubscriber(ObjectMapper objectMapper, AlertBroadcastService alertBroadcastService) {
        this.objectMapper = objectMapper;
        this.alertBroadcastService = alertBroadcastService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        log.info("Received Redis safety event from channel {}: {}", channel, payload);

        try {
            SafetyEventDto event = objectMapper.readValue(payload, SafetyEventDto.class);
            alertBroadcastService.broadcast(event);
        } catch (JsonProcessingException ex) {
            log.error("Failed to parse Redis safety event JSON: payload={}", payload, ex);
        } catch (RuntimeException ex) {
            log.error("Failed to broadcast Redis safety event: payload={}", payload, ex);
        }
    }
}
