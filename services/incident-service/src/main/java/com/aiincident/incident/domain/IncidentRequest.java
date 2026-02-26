package com.aiincident.incident.domain;

import jakarta.validation.constraints.NotBlank;

public record IncidentRequest(
        @NotBlank String incidentKey,
        @NotBlank String serviceName,
        @NotBlank String severity,
        @NotBlank String summary
) {}
