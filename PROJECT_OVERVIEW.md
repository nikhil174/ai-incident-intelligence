# AI Incident Intelligence Platform

> A production-grade, event-driven microservices platform that ingests system events, detects anomalies using statistical AI models, and manages incidents — built to demonstrate backend engineering, distributed systems design, and DevOps expertise.

---

## Table of Contents

- [About](#about)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Service Breakdown](#service-breakdown)
- [Data Flow](#data-flow)
- [How to Run](#how-to-run)
- [API Reference & Demo](#api-reference--demo)
- [Key Engineering Decisions](#key-engineering-decisions)
- [CI/CD Pipeline](#cicd-pipeline)
- [Project Structure](#project-structure)

---

## About

Modern distributed systems generate millions of events per hour — logs, traces, alerts. Manual triage is slow, noisy, and error-prone. This platform solves that by:

1. **Ingesting** raw events via a REST API and streaming them through Kafka.
2. **Enriching** events with severity scoring and persisting to MongoDB + Elasticsearch.
3. **Managing incidents** with CRUD APIs backed by MySQL and Redis caching.
4. **Scoring anomalies** using a Python AI service with z-score-based statistical analysis.

---

## Architecture

```
                                ┌─────────────────────────────┐
                                │        Client / curl        │
                                └──────┬──────────────────────┘
                                       │  POST /api/events
                                       ▼
                           ┌───────────────────────┐
                           │  Event Ingestion Svc   │  (Java · Spring Boot)
                           │       :6001            │
                           └───────────┬───────────┘
                                       │  Kafka → raw-events
                                       ▼
                           ┌───────────────────────┐
                           │   Processor Service    │  (Java · Spring Boot)
                           │       :6002            │
                           └──┬────────┬────────┬──┘
                              │        │        │
                    ┌─────────▼──┐ ┌───▼────┐ ┌─▼───────────────┐
                    │  MongoDB   │ │  ES    │ │ Kafka            │
                    │ (raw docs) │ │(search)│ │ enriched-events  │
                    └────────────┘ └────────┘ └────────┬────────┘
                                                       │
                           ┌───────────────────────────▼───────┐
                           │       Incident Service            │  (Java · Spring Boot)
                           │           :6003                   │
                           └──────┬────────────┬───────────────┘
                                  │            │
                            ┌─────▼───┐  ┌─────▼───┐
                            │  MySQL  │  │  Redis  │
                            │ (CRUD)  │  │ (cache) │
                            └─────────┘  └─────────┘

                           ┌───────────────────────┐
                           │  AI Analysis Service   │  (Python · FastAPI)
                           │       :6004            │
                           └───────────────────────┘
                              Stateless anomaly scoring
                              via z-score + severity model
```

---

## Tech Stack

| Layer            | Technology                                         |
| ---------------- | -------------------------------------------------- |
| **Backend**      | Java 17, Spring Boot 3.3, Spring Data JPA, Kafka   |
| **AI Service**   | Python 3.11, FastAPI, Pydantic, NumPy              |
| **Messaging**    | Apache Kafka (Confluent 7.5) with Zookeeper        |
| **Databases**    | MySQL 8.4 (relational), MongoDB 7.0 (document)     |
| **Search**       | Elasticsearch 8.15 (full-text event search)        |
| **Caching**      | Redis 7.2 (incident lookup caching via `@Cacheable`) |
| **DevOps**       | Docker, Docker Compose, Jenkins CI/CD              |
| **Observability**| Spring Actuator (`/health`, `/metrics`, `/info`)   |

---

## Service Breakdown

### 1. Event Ingestion Service — `:6001`

| Aspect      | Detail                                                          |
| ----------- | --------------------------------------------------------------- |
| Language    | Java 17 · Spring Boot 3                                        |
| Purpose     | REST gateway — accepts raw events and publishes to Kafka        |
| Key Classes | `EventController` → validates request → `EventPublisherService` → Kafka `raw-events` topic |
| Input       | JSON with `serviceName`, `severity`, `message`, `traceId`, `tags` |
| Output      | Async acknowledgement + Kafka message keyed by `traceId`        |

### 2. Processor Service — `:6002`

| Aspect      | Detail                                                          |
| ----------- | --------------------------------------------------------------- |
| Language    | Java 17 · Spring Boot 3                                        |
| Purpose     | Consumes raw events, enriches with anomaly scores, persists     |
| Key Classes | `RawEventConsumer` → `EnrichmentService` (severity-based scoring) → `PersistenceService` |
| Stores To   | **MongoDB** (raw event documents), **Elasticsearch** (searchable index), **Kafka** `enriched-events` topic |

### 3. Incident Service — `:6003`

| Aspect      | Detail                                                          |
| ----------- | --------------------------------------------------------------- |
| Language    | Java 17 · Spring Boot 3                                        |
| Purpose     | Incident lifecycle management (Create, Read, Update status)     |
| Key Classes | `IncidentController` → `IncidentService` → `IncidentRepository` (JPA) |
| Features    | MySQL persistence, Redis `@Cacheable` for reads, composite DB index on `(status, severity, updatedAt)` |

### 4. AI Analysis Service — `:6004`

| Aspect      | Detail                                                          |
| ----------- | --------------------------------------------------------------- |
| Language    | Python 3.11 · FastAPI                                          |
| Purpose     | Stateless anomaly scoring and root-cause suggestion             |
| Algorithm   | Weighted composite: **40%** latency z-score + **30%** error rate + **30%** severity factor |
| Output      | `anomaly_score`, `summary`, `probable_root_cause`, `remediation` steps |

---

## Data Flow

```
1.  Client sends POST /api/events to Ingestion Service
2.  Ingestion validates & publishes to Kafka topic "raw-events" (keyed by traceId)
3.  Processor consumes from "raw-events":
      a. Enriches payload with severity-based anomaly score
      b. Saves to MongoDB (raw_events collection)
      c. Indexes into Elasticsearch (events-index)
      d. Publishes enriched event to Kafka topic "enriched-events"
4.  Incident Service manages incident lifecycle via REST APIs (MySQL + Redis)
5.  AI Analysis Service can be called on-demand to score any event for anomalies
```

---

## How to Run

### Prerequisites

- **Docker Desktop** (Docker Engine + Docker Compose)
- Ports `6001–6004` available on localhost

### Start (single command)

```bash
cd projects/ai-incident-intelligence
docker compose up -d --build
```

This spins up **10 containers**: 4 application services + Kafka + Zookeeper + MySQL + MongoDB + Redis + Elasticsearch.

### Verify All Services Are Healthy

```bash
# Event Ingestion
curl http://localhost:6001/actuator/health    # {"status":"UP"}

# Processor
curl http://localhost:6002/actuator/health    # {"status":"UP"}

# Incident Service
curl http://localhost:6003/actuator/health    # {"status":"UP"}

# AI Analysis
curl http://localhost:6004/health             # {"status":"ok"}
```

### Stop Everything

```bash
docker compose down
```

---

## API Reference & Demo

### 1. Ingest an Event

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

**Response:**
```json
{
  "status": "ACCEPTED",
  "traceId": "abc-123",
  "timestamp": "2026-03-03T10:15:30.123Z"
}
```

### 2. Create an Incident

```bash
curl -X POST http://localhost:6003/api/incidents \
  -H 'Content-Type: application/json' \
  -d '{
    "incidentKey": "INC-001",
    "serviceName": "checkout-service",
    "severity": "HIGH",
    "summary": "Persistent timeout in checkout flow"
  }'
```

### 3. List Open Incidents

```bash
curl http://localhost:6003/api/incidents?status=OPEN
```

### 4. Update Incident Status

```bash
curl -X PATCH http://localhost:6003/api/incidents/1/status \
  -H 'Content-Type: application/json' \
  -d '{"status": "RESOLVED"}'
```

### 5. AI Anomaly Analysis

```bash
curl -X POST http://localhost:6004/analyze \
  -H 'Content-Type: application/json' \
  -d '{
    "service_name": "checkout-service",
    "severity": "HIGH",
    "message": "Timeout spikes",
    "recent_latencies_ms": [120, 130, 125, 450, 980],
    "error_rate": 0.35
  }'
```

**Response:**
```json
{
  "anomaly_score": 0.593,
  "summary": "checkout-service shows anomaly score 0.593. Severity=HIGH, error_rate=0.35.",
  "probable_root_cause": "Transient service degradation",
  "remediation": [
    "Check p95/p99 latency and DB connection pool usage",
    "Inspect recent deploys and dependency health",
    "Scale affected service and enable circuit-breaker policy"
  ]
}
```

---

## Key Engineering Decisions

| Decision | Rationale |
| --- | --- |
| **Kafka as event backbone** | Decouples producers from consumers, enables replay, and supports back-pressure — essential for high-throughput event streams |
| **Polyglot persistence** (MySQL + MongoDB + Elasticsearch + Redis) | Each store is used for what it's best at: relational integrity, flexible documents, full-text search, and low-latency caching |
| **Polyglot services** (Java + Python) | Java/Spring Boot for robust backend services; Python/FastAPI for lightweight ML/statistical workloads — demonstrates cross-language microservice communication |
| **traceId as Kafka partition key** | Ensures all events from the same trace land in the same partition → ordering guarantee per trace |
| **Z-score based anomaly detection** | Lightweight statistical approach for latency spike detection without heavy ML dependencies; easily replaceable with a trained model |
| **Composite DB index** on `(status, severity, updatedAt)` | Optimizes the most common query pattern for incident dashboards |
| **Redis `@Cacheable`** | Prevents repeated DB hits for frequently accessed incident lookups |
| **Multi-stage Docker builds** | Keeps production images small (JRE-only, no build tools) |

---

## CI/CD Pipeline

The project includes a **Jenkinsfile** with a 5-stage pipeline:

```
Checkout → Unit Tests → Build Docker Images → Deploy → Smoke Tests
```

- Each Java service is tested independently with `mvn test`.
- Docker images are tagged with the Jenkins build number (`$BUILD_NUMBER`).
- Deploy stage is templated for AWS (ECS/EC2) deployment scripts.

---

## Project Structure

```
ai-incident-intelligence/
├── docker-compose.yml              # Orchestrates all 10 containers
├── Jenkinsfile                     # CI/CD pipeline definition
├── PROJECT_OVERVIEW.md             # ← You are here
├── README.md
├── docs/
├── infrastructure/
│   └── mysql-init.sql              # DB + user bootstrap script
└── services/
    ├── event-ingestion-service/    # Java · REST → Kafka producer
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/java/com/aiincident/ingestion/
    │       ├── api/EventController.java
    │       ├── config/KafkaProducerConfig.java
    │       ├── domain/EventRequest.java
    │       └── service/EventPublisherService.java
    │
    ├── processor-service/          # Java · Kafka consumer → Mongo + ES
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/java/com/aiincident/processor/
    │       ├── config/KafkaConfig.java
    │       ├── consumer/RawEventConsumer.java
    │       ├── domain/EventEnvelope.java
    │       └── service/{EnrichmentService, PersistenceService}.java
    │
    ├── incident-service/           # Java · MySQL + Redis CRUD
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/java/com/aiincident/incident/
    │       ├── api/IncidentController.java
    │       ├── domain/{Incident, IncidentRequest}.java
    │       ├── repository/IncidentRepository.java
    │       └── service/IncidentService.java
    │
    └── ai-analysis-service/        # Python · FastAPI anomaly scorer
        ├── Dockerfile
        ├── requirements.txt
        └── app/main.py
```

---

## Contact

**Nikhil Srivastava** — [GitHub](https://github.com/nikhil174)
