package com.example.serviceA;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class KafkaResponseConsumer {

    private final List<String> responses = new CopyOnWriteArrayList<>();

    public List<String> getAllResponses() {
        return responses;
    }

    @KafkaListener(topics = "${kafka.topic.response}", groupId = "${kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        String message = record.value();
        Header traceHeader = record.headers().lastHeader("traceId");
        String traceId = traceHeader != null ? new String(traceHeader.value(), StandardCharsets.UTF_8) : null;
        if (traceId != null) {
            MDC.put("traceId", traceId);
        }
        log.info("Step 4: Received message from response-topic: {} with traceId: {}", message, traceId);
        responses.add(message);
        if (traceId != null) {
            MDC.remove("traceId");
        }
    }
}
