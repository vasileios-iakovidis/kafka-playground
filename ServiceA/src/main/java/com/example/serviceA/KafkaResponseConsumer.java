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
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class KafkaResponseConsumer {

    private final ConcurrentHashMap<String, Sinks.One<String>> responseSinks = new ConcurrentHashMap<>();

    public Mono<String> awaitResponse(String correlationId) {
        Sinks.One<String> sink = Sinks.one();
        responseSinks.put(correlationId, sink);
        log.info("Step 4: Await for response");
        return sink.asMono();
    }

    @KafkaListener(topics = "${kafka.topic.response}", groupId = "${kafka.consumer.group-id}")
    public void listen(ConsumerRecord<String, String> record) {
        String message = record.value();
        Header correlationHeader = record.headers().lastHeader("correlationId");
        String correlationId = correlationHeader != null ? new String(correlationHeader.value(), StandardCharsets.UTF_8) : null;
        Header traceHeader = record.headers().lastHeader("traceId");
        String traceId = traceHeader != null ? new String(traceHeader.value(), StandardCharsets.UTF_8) : null;
        if (traceId != null) {
            MDC.put("traceId", traceId);
        }
        log.info("Step 5: Received message from response-topic: {} with correlationId: {} and traceId: {}", message, correlationId, traceId);
        if (correlationId != null && responseSinks.containsKey(correlationId)) {
            responseSinks.remove(correlationId).tryEmitValue(message);
        }
        if (traceId != null) {
            MDC.remove("traceId");
        }
    }
}
