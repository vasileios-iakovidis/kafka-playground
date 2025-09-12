package com.example.serviceB;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class KafkaRequestConsumer {
    private final KafkaResponseProducer responseProducer;

    public KafkaRequestConsumer(KafkaResponseProducer responseProducer) {
        this.responseProducer = responseProducer;
    }

    @KafkaListener(topics = "${kafka.topic.request}", groupId = "${kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        String message = record.value();
        Header correlationHeader = record.headers().lastHeader("correlationId");
        String correlationId = correlationHeader != null ? new String(correlationHeader.value(), StandardCharsets.UTF_8) : null;
        Header traceHeader = record.headers().lastHeader("traceId");
        String traceId = traceHeader != null ? new String(traceHeader.value(), StandardCharsets.UTF_8) : null;
        if (correlationId == null || traceId == null) {
            log.warn("Missing correlationId or traceId in incoming message. Skipping processing. message={}", message);
            return;
        }
        MDC.put("traceId", traceId);
        log.info("Service B: Step 1: Received request: {} with correlationId: {} and traceId: {}", message, correlationId, traceId);
        // Simulate processing and produce a response
        String response = "Processed: " + message;
        responseProducer.sendResponse(response, correlationId, traceId);
        log.info("Service B: Step 2: Sent response: {} with correlationId: {} and traceId: {}", response, correlationId, traceId);
        MDC.remove("traceId");
    }
}
