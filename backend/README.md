# AI E-Learning Backend

Spring Boot modular monolith foundation for the AI-powered e-learning SaaS.

This skeleton intentionally includes only foundation code:

- base modules and package boundaries
- PostgreSQL + pgvector migration
- Redis, RabbitMQ, MinIO configuration
- JWT security configuration
- OpenAPI/Swagger
- initial `User`, `Subject`, and `Material` entities

It does not implement full learning workflows, RAG orchestration, recommendations, or advanced file processing yet.

Local boot verification instructions are in `../docs/local-backend-boot.md`.
