# Scaling Strategy

## Horizontal Scale
- Scale ingestion and processor consumers independently based on Kafka lag.
- Scale incident service based on API latency and CPU.

## Bottlenecks and Mitigations
- Kafka partition under-allocation: increase partitions per high-volume service.
- Elasticsearch indexing pressure: batch writes and tune refresh interval.
- MySQL write contention: use connection pools and selective indexing.

## AWS Direction
- ECS Fargate for stateless services
- Managed Kafka (MSK), OpenSearch, ElastiCache, RDS, DocumentDB/Mongo-compatible cluster
