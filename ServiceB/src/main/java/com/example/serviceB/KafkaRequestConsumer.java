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
        Header traceHeader = record.headers().lastHeader("traceId");
        String traceId = traceHeader != null ? new String(traceHeader.value(), StandardCharsets.UTF_8) : null;
        if (traceId == null) {
            log.warn("Missing traceId in incoming message. message={}", message);
        }
        MDC.put("traceId", traceId);
        log.info("Service B: Step 1: Received request: {} with traceId: {}", message, traceId);
        // Simulate processing and produce a response
        String response = "Processed: " + message;
        responseProducer.sendResponse(response, traceId);
        log.info("Service B: Step 2: Sent response: {} with traceId: {}", response, traceId);
        MDC.remove("traceId");
    }
}
