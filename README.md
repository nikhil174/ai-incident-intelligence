# AI Incident Intelligence Platform

A backend + AI + DevOps focused project for SDE interviews.

## Problem
Modern systems generate huge log and event streams. Manual triage is slow and noisy. This platform ingests events, detects anomalies, groups incidents, and provides AI-generated root-cause summaries.

## Tech Stack
- Backend: Java 17, Spring Boot 3
- AI: Python 3.11, FastAPI
- Messaging: Kafka
- Datastores: MySQL, MongoDB, Redis, Elasticsearch
- DevOps: Docker Compose, Jenkins, AWS-ready deployment templates

## Monorepo Layout
```text
projects/ai-incident-intelligence
├── docs
├── infrastructure
├── services
│   ├── event-ingestion-service
│   ├── processor-service
│   ├── incident-service
│   └── ai-analysis-service
├── docker-compose.yml
└── Jenkinsfile
```

## Architecture
1. `event-ingestion-service` receives events and publishes to Kafka topic `raw-events`.
2. `processor-service` consumes `raw-events`, enriches payloads, stores raw records in MongoDB, indexes searchable records in Elasticsearch, and emits `enriched-events`.
3. `incident-service` creates/updates incidents from enriched events and exposes CRUD APIs.
4. `ai-analysis-service` scores anomalies and returns incident summaries/remediation suggestions.

## Local Run
### Prerequisites
- Docker + Docker Compose
- Java 17
- Maven 3.9+
- Python 3.11+

### Start infra and apps
```bash
docker compose up -d
```

### Health checks
- Ingestion: `http://localhost:6001/actuator/health`
- Processor: `http://localhost:6002/actuator/health`
- Incident: `http://localhost:6003/actuator/health`
- AI service: `http://localhost:6004/health`

## Example API Flow
1. Post an event to ingestion:
```bash
curl -X POST http://localhost:6001/api/events \
  -H 'Content-Type: application/json' \
  -d '{
    "serviceName": "checkout-service",
    "severity": "HIGH",
    "message": "Timeout spikes while writing order",
    "traceId": "abc-123",
    "tags": ["checkout", "mysql"]
  }'
```
2. Search incidents:
```bash
curl http://localhost:6003/api/incidents?status=OPEN
```

## Resume-ready Highlights
- Event-driven microservices with Kafka, retry, and dead-letter queues.
- Polyglot persistence: MySQL + MongoDB + Redis + Elasticsearch.
- AI-assisted anomaly scoring and root-cause summaries.
- CI/CD pipeline with tests, image build, and deployment stage.
- Observability with health checks, structured logs, and metrics.

## Roadmap Enhancements
- Multi-tenant RBAC with OIDC.
- Auto-remediation runbooks.
- Drift monitoring for anomaly models.
- Kubernetes + Helm deployment.
- SLO/error budget based alerting.
