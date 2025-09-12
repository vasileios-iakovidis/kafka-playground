package com.example.serviceA;

import java.util.UUID;
import java.time.Duration;

import org.springframework.stereotype.Service;
import org.slf4j.MDC;

import reactor.core.publisher.Mono;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class ProcessMessageService {

    private final KafkaPublisher publisher;
    private final KafkaResponseConsumer responseConsumer;

    public Mono<String> processMessage(String message) {
        validateMessage(message);
        String correlationId = UUID.randomUUID().toString();
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
            MDC.put("traceId", traceId);
        }
        return publisher.publishToRequestTopic(message, correlationId, traceId)
                .then(responseConsumer.awaitResponse(correlationId)
                    .timeout(Duration.ofSeconds(8), Mono.just("TIMEOUT_RESPONSE")));
    }

    private void validateMessage(String message) {
        log.info("Step 2: Validating message: {}", message);
        if (message.contains("ok")) {
            throw new IllegalArgumentException("Message shouldn't include `ok`");
        }
    }
}
