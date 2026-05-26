package com.strange.safety.config;

import com.strange.safety.event.SafetyEventSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    @Bean
    public ChannelTopic safetyEventsTopic(@Value("${app.redis.channel:safety-events}") String channelName) {
        return new ChannelTopic(channelName);
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            SafetyEventSubscriber safetyEventSubscriber,
            ChannelTopic safetyEventsTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(safetyEventSubscriber, safetyEventsTopic);
        return container;
    }
}
