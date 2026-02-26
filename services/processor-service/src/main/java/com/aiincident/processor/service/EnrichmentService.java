package com.aiincident.processor.service;

import com.aiincident.processor.domain.EventEnvelope;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
public class EnrichmentService {
    public EventEnvelope enrich(Map<String, Object> raw) {
        String severity = String.valueOf(raw.getOrDefault("severity", "LOW"));
        double score = switch (severity.toUpperCase()) {
            case "CRITICAL" -> 0.95;
            case "HIGH" -> 0.8;
            case "MEDIUM" -> 0.5;
            default -> 0.2;
        };
        return new EventEnvelope(
                String.valueOf(raw.getOrDefault("serviceName", "unknown-service")),
                severity,
                String.valueOf(raw.getOrDefault("message", "")),
                String.valueOf(raw.getOrDefault("traceId", "")),
                (java.util.List<String>) raw.getOrDefault("tags", java.util.List.of("unknown")),
                Instant.now(),
                score
        );
    }
}
