package com.example.serviceA;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class MessageController {

    private final ProcessMessageService service;
    private final KafkaResponseConsumer responseConsumer;

    @PostMapping
    public Mono<ResponseEntity<String>> getResponse(@RequestBody String request) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("traceId", traceId);

        log.info("Step 1: Request received {}", request);

        return service.processMessage(request)
                .map(ResponseEntity::ok)
                .doFinally(signalType -> MDC.remove("traceId"));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<List<String>>> getAllResponses() {
        List<String> responses = responseConsumer.getAllResponses();
        return Mono.just(ResponseEntity.ok(responses));
    }
}
