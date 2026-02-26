package com.aiincident.processor.consumer;

import com.aiincident.processor.domain.EventEnvelope;
import com.aiincident.processor.service.EnrichmentService;
import com.aiincident.processor.service.PersistenceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RawEventConsumer {
    private final EnrichmentService enrichmentService;
    private final PersistenceService persistenceService;

    public RawEventConsumer(EnrichmentService enrichmentService, PersistenceService persistenceService) {
        this.enrichmentService = enrichmentService;
        this.persistenceService = persistenceService;
    }

    @KafkaListener(topics = "${app.kafka.raw-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(Map<String, Object> payload) {
        EventEnvelope enriched = enrichmentService.enrich(payload);
        persistenceService.persist(enriched);
    }
}
