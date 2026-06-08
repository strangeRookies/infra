package com.strange.safety.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;

@Configuration
@IntegrationComponentScan
public class MqttConfig {

    @Value("${mqtt.broker-url}")
    private String brokerUrl;

    @Value("${mqtt.client-id}")
    private String clientId;

    // 1. EMQX 브로커 연결 옵션 설정 (대장님이 열어주신 주소로 세팅)
    @Bean
    public MqttConnectOptions mqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[] { brokerUrl });
        // ID/PW 인증이 필요하다면 여기에 추가 (현재는 익명 접속)
        // options.setUserName("admin");
        // options.setPassword("password".toCharArray());
        options.setAutomaticReconnect(true); // 끊기면 자동 재접속
        return options;
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions());
        return factory;
    }

    // 2. 메시지 수신(구독) 채널 어댑터 설정 -> ★ 이 코드가 실행될 때 대시보드에 Client ID가 뜹니다!
    @Bean
    public MessageProducer inbound() {
        // AI 파트와 약속한 토픽(채널)을 구독합니다. (예: safety/alert/#)
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId,
                mqttClientFactory(), "safety/alert/#");

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1); // 메시지 전송 보장 수준
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 3. 수신된 메시지를 백엔드 로직으로 전달할 파이프라인
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
}