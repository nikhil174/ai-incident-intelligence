package com.aiincident.ingestion.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EventRequest(
        @NotBlank String serviceName,
        @NotBlank String severity,
        @NotBlank String message,
        @NotBlank String traceId,
        @NotEmpty List<String> tags
) {}
