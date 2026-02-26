package com.aiincident.incident.api;

import com.aiincident.incident.domain.Incident;
import com.aiincident.incident.domain.IncidentRequest;
import com.aiincident.incident.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<Incident> create(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.create(request));
    }

    @GetMapping
    public List<Incident> list(@RequestParam(required = false) String status) {
        return incidentService.list(status);
    }

    @GetMapping("/{id}")
    public Incident get(@PathVariable Long id) {
        return incidentService.get(id);
    }

    @PatchMapping("/{id}/status")
    public Incident updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return incidentService.updateStatus(id, payload.getOrDefault("status", "OPEN"));
    }
}
