package com.aiincident.ingestion.api;

import com.aiincident.ingestion.domain.EventRequest;
import com.aiincident.ingestion.service.EventPublisherService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventPublisherService publisherService;

    public EventController(EventPublisherService publisherService) {
        this.publisherService = publisherService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingest(@Valid @RequestBody EventRequest request) {
        publisherService.publish(request);
        return ResponseEntity.accepted().body(Map.of(
                "status", "ACCEPTED",
                "traceId", request.traceId(),
                "timestamp", Instant.now().toString()
        ));
    }
}
