package com.example.serviceB;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class KafkaResponseProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String responseTopic;

    public KafkaResponseProducer(KafkaTemplate<String, String> kafkaTemplate,
                                 @Value("${kafka.topic.response}") String responseTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.responseTopic = responseTopic;
    }

    public void sendResponse(String message, String correlationId, String traceId) {
        ProducerRecord<String, String> record = new ProducerRecord<>(responseTopic, message);
        if (correlationId != null) {
            record.headers().add(new RecordHeader("correlationId", correlationId.getBytes(StandardCharsets.UTF_8)));
        }
        if (traceId != null) {
            record.headers().add(new RecordHeader("traceId", traceId.getBytes(StandardCharsets.UTF_8)));
        }
        kafkaTemplate.send(record);
    }

    // Optionally, keep the old method for compatibility
    public void sendResponse(String message, String correlationId) {
        sendResponse(message, correlationId, null);
    }
}
