package com.example.serviceA;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class KafkaPublisher {

    private final String requestTopic;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaPublisher(@Value("${kafka.topic.name}") String requestTopic,
                          KafkaTemplate<String, String> kafkaTemplate) {
        this.requestTopic = requestTopic;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> publishToRequestTopic(String message) {
        log.info("Step 3: Publishing the message to request topic: {}", message);
        String traceId = MDC.get("traceId");
        ProducerRecord<String, String> record = new ProducerRecord<>(requestTopic, message);
        if (traceId != null) {
            record.headers().add(new RecordHeader("traceId", traceId.getBytes(StandardCharsets.UTF_8)));
        }
        return Mono.fromFuture(() -> kafkaTemplate.send(record)).then();
    }

}