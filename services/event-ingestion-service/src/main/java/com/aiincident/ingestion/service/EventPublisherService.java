package com.aiincident.ingestion.service;

import com.aiincident.ingestion.domain.EventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventPublisherService {
    private final KafkaTemplate<String, EventRequest> kafkaTemplate;
    private final String topic;

    public EventPublisherService(KafkaTemplate<String, EventRequest> kafkaTemplate,
                                 @Value("${app.kafka.raw-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(EventRequest event) {
        kafkaTemplate.send(topic, event.traceId(), event);
    }
}
