package com.example.serviceA;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Slf4j
public class MessageController {

    private final ProcessMessageService service;

    @PostMapping
    public Mono<ResponseEntity<String>> getResponse(@RequestBody String request) {
        String traceId = Optional.ofNullable(MDC.get("traceId"))
                .orElse(UUID.randomUUID().toString());
        MDC.put("traceId", traceId);

        log.info("Step 1: Request received {}", request);

        return service.processMessage(request)
                .map(ResponseEntity::ok);
    }
}
