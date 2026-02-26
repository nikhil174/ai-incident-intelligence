# CI/CD Pipeline

## Jenkins Stages
1. Checkout
2. Unit tests
3. Build service artifacts
4. Build Docker images
5. Push images to registry
6. Deploy to AWS target (ECS/EC2)
7. Smoke test health endpoints

## Rollback Strategy
- Deploy by immutable image tag
- Keep last known-good image
- One-click rollback to previous tag

## Quality Gates
- Failing tests block deployment
- Basic SAST/dependency scan hook can be attached before image push
