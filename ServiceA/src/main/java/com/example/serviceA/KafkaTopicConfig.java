package com.example.serviceA;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Value("${kafka.topic.name}")
    private String requestTopic;

    @Value("${kafka.topic.response}")
    private String responseTopic;

    @Bean
    public NewTopic createRequestTopic() {
        return new NewTopic(requestTopic, 1, (short) 1);
    }

    @Bean
    public NewTopic createResponseTopic() {
        return new NewTopic(responseTopic, 1, (short) 1);
    }
}
