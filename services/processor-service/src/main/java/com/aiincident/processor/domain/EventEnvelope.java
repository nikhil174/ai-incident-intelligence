package com.aiincident.processor.domain;

import java.time.Instant;
import java.util.List;

public record EventEnvelope(
        String serviceName,
        String severity,
        String message,
        String traceId,
        List<String> tags,
        Instant receivedAt,
        double anomalyScore
) {}
