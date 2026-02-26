package com.aiincident.processor.service;

import com.aiincident.processor.domain.EventEnvelope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class PersistenceService {
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final RestClient restClient;
    private final String enrichedTopic;

    public PersistenceService(org.springframework.data.mongodb.core.MongoTemplate mongoTemplate,
                              KafkaTemplate<String, EventEnvelope> kafkaTemplate,
                              @Value("${app.kafka.enriched-topic}") String enrichedTopic,
                              @Value("${app.elasticsearch.url}") String elasticsearchUrl) {
        this.mongoTemplate = mongoTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.enrichedTopic = enrichedTopic;
        this.restClient = RestClient.builder().baseUrl(elasticsearchUrl).build();
    }

    public void persist(EventEnvelope envelope) {
        mongoTemplate.save(envelope, "raw_events");
        indexToElasticsearch(envelope);
        kafkaTemplate.send(enrichedTopic, envelope.traceId(), envelope);
    }

    private void indexToElasticsearch(EventEnvelope envelope) {
        try {
            restClient.post()
                    .uri("/events-index/_doc")
                    .body(Map.of(
                            "serviceName", envelope.serviceName(),
                            "severity", envelope.severity(),
                            "message", envelope.message(),
                            "traceId", envelope.traceId(),
                            "tags", envelope.tags(),
                            "timestamp", envelope.receivedAt().toString(),
                            "anomalyScore", envelope.anomalyScore()
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ignored) {
            // Elasticsearch write failures should not block pipeline in this starter implementation.
        }
    }
}
