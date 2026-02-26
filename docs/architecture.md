# Architecture Design

## Design Goals
- High-throughput event ingestion
- Resilient asynchronous processing
- Fast search and triage
- AI support for incident analysis

## Container-Level View
- `event-ingestion-service` (Spring Boot): validates incoming events and publishes to Kafka.
- `processor-service` (Spring Boot): enriches/normalizes events, writes to MongoDB + Elasticsearch, forwards enriched events.
- `incident-service` (Spring Boot): incident lifecycle APIs and persistence in MySQL, caching in Redis.
- `ai-analysis-service` (FastAPI): anomaly scoring and summarization.

## Data Stores
- MySQL: incidents, assignments, SLA metadata
- MongoDB: raw payload archive
- Redis: hot incident cache / dedup keys
- Elasticsearch: full-text search and analytics facets

## Event Topics
- `raw-events`
- `enriched-events`
- `incident-dlq`

## Failure Handling
- Consumer retry with backoff
- Dead-letter topic for poison events
- Idempotency using trace + timestamp hashing in Redis

## Security
- JWT auth on management APIs
- Input validation and request size limits
- Secrets via environment variables / AWS Secrets Manager in cloud deployments
