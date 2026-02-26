# Data Model

## MySQL (Incident Service)
### `incidents`
- `id` (BIGINT, PK)
- `incident_key` (VARCHAR, unique)
- `service_name` (VARCHAR)
- `status` (VARCHAR) - OPEN, INVESTIGATING, RESOLVED
- `severity` (VARCHAR) - LOW, MEDIUM, HIGH, CRITICAL
- `summary` (TEXT)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

Indexes:
- `(status, severity, updated_at)`
- `incident_key` unique index

## MongoDB
Collection: `raw_events`
- source payload + ingest metadata

## Elasticsearch
Index: `events-index`
- searchable fields: `serviceName`, `message`, `severity`, `tags`, `timestamp`
