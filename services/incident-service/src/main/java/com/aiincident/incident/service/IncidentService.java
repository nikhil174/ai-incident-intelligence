package com.aiincident.incident.service;

import com.aiincident.incident.domain.Incident;
import com.aiincident.incident.domain.IncidentRequest;
import com.aiincident.incident.repository.IncidentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {
    private final IncidentRepository incidentRepository;

    public IncidentService(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    public Incident create(IncidentRequest request) {
        Incident incident = new Incident();
        incident.setIncidentKey(request.incidentKey());
        incident.setServiceName(request.serviceName());
        incident.setSeverity(request.severity());
        incident.setSummary(request.summary());
        incident.setStatus("OPEN");
        return incidentRepository.save(incident);
    }

    public List<Incident> list(String status) {
        if (status == null || status.isBlank()) {
            return incidentRepository.findAll();
        }
        return incidentRepository.findByStatus(status);
    }

    @Cacheable(cacheNames = "incidentById", key = "#id")
    public Incident get(Long id) {
        return incidentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Incident not found: " + id));
    }

    public Incident updateStatus(Long id, String status) {
        Incident incident = get(id);
        incident.setStatus(status);
        return incidentRepository.save(incident);
    }
}
