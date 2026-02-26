package com.aiincident.incident.repository;

import com.aiincident.incident.domain.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByIncidentKey(String incidentKey);
    List<Incident> findByStatus(String status);
}
