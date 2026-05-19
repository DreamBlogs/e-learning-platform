# AI-Powered E-Learning SaaS Architecture Foundation

## 0. Architecture Principles

This project starts as an MVP-first modular monolith.

Core decisions:

- Backend: Java Spring Boot modular monolith.
- Frontend: Next.js with TypeScript.
- Database: PostgreSQL.
- Vector search: pgvector inside PostgreSQL.
- Cache/session/short-lived state: Redis.
- File storage: MinIO locally, S3-compatible storage in production.
- Queue: RabbitMQ.
- AI pattern: RAG over user subject materials, learning history, tests, and knowledge model.
- No microservices at MVP stage.

The main product value is not "chat with files". The product value is long-term learning intelligence: tracking what the learner knows, what they forget, what they repeatedly miss, and what should be reviewed next.

## 1. High-Level System Architecture

```text
User
  |
  v
Next.js Web App
  |
  v
Spring Boot Modular Monolith
  |
  +--> PostgreSQL + pgvector
  |      - transactional data
  |      - subject data
  |      - knowledge model
  |      - assessments
  |      - embeddings
  |
  +--> Redis
  |      - cache
  |      - rate limits
  |      - temporary AI/session state
  |
  +--> MinIO/S3
  |      - original uploads
  |      - extracted artifacts
  |      - generated exports later
  |
  +--> RabbitMQ
  |      - file processing jobs
  |      - embedding generation jobs
  |      - test generation jobs
  |      - analytics recalculation jobs
  |
  +--> AI Provider / LLM Gateway
         - chat completions
         - summaries
         - extraction
         - quiz generation
         - feedback generation
```

Runtime model:

1. The frontend calls the Spring Boot API.
2. The backend owns all business rules and persistence.
3. Uploaded files are stored in MinIO/S3 first.
4. Heavy processing is queued through RabbitMQ.
5. Workers are not separate microservices at MVP; they are Spring Boot consumers inside the same deployable application.
6. PostgreSQL stores canonical data and pgvector embeddings.
7. RAG retrieves relevant chunks from one subject scope and combines them with the learner's knowledge model.

## 2. Backend Modules Structure

The backend should be organized by business capability, not by technical layer only.

Recommended modules:

```text
auth
  User accounts, sessions, roles, tenant/user ownership.

subjects
  Subjects such as Java, Math, Physics.
  Subject home page data and ownership boundaries.

materials
  File metadata, upload lifecycle, extracted text, processing status.

content
  Chunks, summaries, concepts, generated notes, flashcards.

rag
  Retrieval, prompt assembly, citations, context filtering.

chat
  Subject-scoped AI chat, conversations, messages, AI answer storage.

assessments
  Mini-tests, questions, answers, grading, explanations.

knowledge
  Learner topic mastery, weak/strong topics, memory state.

analytics
  GPA, progress trends, learning events, dashboards.

recommendations
  Review recommendations, weak-topic practice suggestions.

processing
  RabbitMQ producers/consumers, async job orchestration.

storage
  MinIO/S3 access, upload URLs, artifact storage.

ai
  LLM client abstraction, embedding client, prompt templates.

common
  shared errors, IDs, auditing, security helpers, pagination.
```

Module dependency rule:

```text
controllers -> application services -> domain model/repositories -> infrastructure
```

Avoid direct cross-module database manipulation. If one module needs behavior from another module, call its application service or publish a domain/application event.

## 3. Frontend Architecture

The frontend should be an application UI, not a marketing site.

Recommended Next.js areas:

```text
app/
  (auth)/
    login/
    register/

  dashboard/
    page.tsx

  subjects/
    page.tsx
    [subjectId]/
      page.tsx
      chat/
      materials/
      tests/
      analytics/
      review/

features/
  auth/
  subjects/
  materials/
  chat/
  tests/
  analytics/
  recommendations/

components/
  ui/
  layout/
  charts/
  upload/

lib/
  api/
  auth/
  query/
  validation/
  utils/

types/
```

Primary frontend screens for MVP:

- Auth screens.
- Main dashboard with subjects, GPA summary, review recommendations.
- Subject page with tabs:
  - Chat
  - Materials
  - Mini-tests
  - Topic analytics
  - Review recommendations
- File upload panel with processing status.
- AI chat with source/citation display.
- Mini-test generation and test-taking flow.
- Results page with mistakes, explanations, updated topic scores.

State management:

- Server state: TanStack Query or equivalent.
- Local UI state: React state/Zustand only where needed.
- Forms: React Hook Form + schema validation.
- Charts: lightweight charting library for progress/GPA trends.

Frontend should not contain learning logic. It should display backend-calculated knowledge scores, recommendations, and analytics.

## 4. Database Entities

Core MVP entities:

```text
users
  id
  email
  password_hash
  display_name
  created_at
  updated_at

subjects
  id
  user_id
  name
  description
  color
  created_at
  updated_at

materials
  id
  subject_id
  user_id
  file_name
  file_type
  storage_key
  size_bytes
  status
  error_message
  created_at
  processed_at

material_artifacts
  id
  material_id
  artifact_type
  storage_key
  content_text
  created_at

content_chunks
  id
  subject_id
  material_id
  chunk_index
  content
  token_count
  metadata_json
  created_at

content_embeddings
  id
  chunk_id
  embedding vector
  embedding_model
  created_at

topics
  id
  subject_id
  name
  description
  parent_topic_id
  created_at

topic_evidence
  id
  topic_id
  source_type
  source_id
  confidence
  created_at

chat_conversations
  id
  subject_id
  user_id
  title
  created_at
  updated_at

chat_messages
  id
  conversation_id
  role
  content
  citations_json
  metadata_json
  created_at

tests
  id
  subject_id
  user_id
  title
  generation_reason
  status
  difficulty
  created_at
  completed_at

test_questions
  id
  test_id
  topic_id
  question_type
  prompt
  options_json
  correct_answer_json
  explanation
  difficulty
  created_at

test_attempts
  id
  test_id
  user_id
  score
  started_at
  submitted_at

test_answers
  id
  attempt_id
  question_id
  answer_json
  is_correct
  ai_feedback
  created_at

knowledge_states
  id
  user_id
  subject_id
  topic_id
  mastery_score
  confidence_score
  last_practiced_at
  next_review_at
  forgetting_risk
  updated_at

learning_events
  id
  user_id
  subject_id
  topic_id
  event_type
  source_type
  source_id
  score_delta
  metadata_json
  created_at

gpa_records
  id
  user_id
  subject_id
  period_type
  period_start
  period_end
  grade_value
  gpa_value
  created_at

recommendations
  id
  user_id
  subject_id
  topic_id
  recommendation_type
  reason
  priority
  status
  due_at
  created_at
```

Important indexes:

- `subjects(user_id)`
- `materials(subject_id, status)`
- `content_chunks(subject_id, material_id)`
- vector index on `content_embeddings.embedding`
- `topics(subject_id, name)`
- `knowledge_states(user_id, subject_id, topic_id)`
- `learning_events(user_id, subject_id, created_at)`
- `recommendations(user_id, subject_id, status, due_at)`

## 5. AI/RAG Pipeline

Subject-scoped RAG is mandatory. The AI must not freely mix data between subjects.

RAG answer flow:

```text
User asks question in subject chat
  |
  v
Classify intent
  |
  +--> explanation / homework help / quiz prep / concept review
  |
  v
Build retrieval query
  |
  v
Retrieve subject chunks from pgvector
  |
  v
Load learner context
  - weak topics
  - recent mistakes
  - current mastery scores
  - recent materials
  |
  v
Assemble prompt
  - system rules
  - subject boundary
  - retrieved chunks
  - learner knowledge state
  - chat history summary
  |
  v
Call LLM
  |
  v
Validate/sanitize response
  |
  v
Store answer, citations, learning event
```

AI answer rules:

- Answers should be grounded in subject materials when the question is about uploaded content.
- If there is insufficient context, the AI should say that the materials do not contain enough information and offer a general explanation only when product rules allow it.
- Every answer that uses materials should include citations to chunks/materials.
- AI interactions can create learning events, but should not aggressively update mastery unless the user answers a question or completes a test.

RAG contexts:

- Material context: extracted text chunks and summaries.
- Learning context: topic mastery, weak topics, recent errors.
- Assessment context: test results and explanations.
- Memory context: topics due for review.

## 6. File Processing Flow

Supported MVP files:

- PDF
- DOCX
- PPTX
- images of notes/homework
- audio/video can be planned but should be limited in MVP unless transcription is essential.

Flow:

```text
User uploads file
  |
  v
Backend creates material row with UPLOADED status
  |
  v
File stored in MinIO/S3
  |
  v
RabbitMQ job: MATERIAL_PROCESS_REQUESTED
  |
  v
Worker extracts text
  |
  +--> PDF/DOCX/PPTX text extraction
  +--> OCR for images
  +--> transcription for audio/video later
  |
  v
Store extracted artifact
  |
  v
Chunk text
  |
  v
Generate embeddings
  |
  v
Extract concepts/topics
  |
  v
Generate summary, notes, optional flashcards
  |
  v
Update material status to PROCESSED
  |
  v
Publish analytics/recommendation recalculation event
```

Material statuses:

```text
UPLOADED
PROCESSING
TEXT_EXTRACTED
EMBEDDING
ANALYZING
PROCESSED
FAILED
```

MVP should store intermediate failures clearly. A failed OCR or embedding job should not corrupt the material record.

## 7. Async Processing Flow

RabbitMQ queues:

```text
material.processing
embedding.generation
content.analysis
test.generation
knowledge.update
recommendation.refresh
```

Event examples:

```text
MaterialUploaded
MaterialTextExtracted
MaterialChunked
EmbeddingsGenerated
TopicsExtracted
TestRequested
TestGenerated
TestSubmitted
KnowledgeStateUpdated
RecommendationsRefreshed
```

Processing rules:

- API requests should create jobs and return quickly.
- Long-running AI calls should run asynchronously unless the user is actively waiting in chat.
- Job handlers should be idempotent.
- Store job status and errors for debugging.
- Use retry with backoff for transient AI/storage failures.
- Use dead-letter queues for repeatedly failing jobs.

For MVP, async consumers can live in the same Spring Boot app:

```text
spring-boot-app
  web controllers
  application services
  rabbitmq producers
  rabbitmq consumers
  scheduled jobs
```

This keeps deployment simple while preserving a clean path to split workers later if needed.

## 8. MVP Scope Boundaries

MVP includes:

- User registration/login.
- Subject creation and subject dashboard.
- Upload materials to a subject.
- Process PDF/DOCX/PPTX/image files into text.
- Chunk text and create embeddings in pgvector.
- Subject-scoped AI chat with citations.
- Generate mini-tests from weak/recent topics.
- Submit tests and receive explanations.
- Maintain topic-level knowledge states.
- Show weak/strong topics.
- Basic GPA dashboard.
- Basic spaced repetition recommendations.

MVP excludes:

- Multi-tenant organization management.
- Teacher/admin classroom workflows.
- Marketplace of courses.
- Real-time collaborative editing.
- Full LMS gradebook replacement.
- Mobile app.
- Complex proctoring.
- Advanced video/audio pipelines unless required after validation.
- Microservices.
- Fine-tuned custom model training.

MVP success criteria:

- A learner can create a subject.
- A learner can upload materials.
- The system can process materials into searchable knowledge.
- Chat answers can use subject context.
- Mini-tests target weak/recent topics.
- Test results update the learner knowledge model.
- The dashboard shows meaningful progress and review recommendations.

## 9. Recommended Spring Boot Modular Monolith Structure

Use package-level modules with clear boundaries.

```text
com.example.learning
  LearningApplication

  auth
    api
    application
    domain
    infrastructure

  subjects
    api
    application
    domain
    infrastructure

  materials
    api
    application
    domain
    infrastructure

  content
    api
    application
    domain
    infrastructure

  rag
    api
    application
    domain
    infrastructure

  chat
    api
    application
    domain
    infrastructure

  assessments
    api
    application
    domain
    infrastructure

  knowledge
    api
    application
    domain
    infrastructure

  analytics
    api
    application
    domain
    infrastructure

  recommendations
    api
    application
    domain
    infrastructure

  processing
    application
    infrastructure

  storage
    application
    infrastructure

  ai
    application
    infrastructure

  common
    api
    application
    domain
    infrastructure
```

Suggested internal convention:

```text
api
  REST controllers, request/response DTOs.

application
  Use cases, commands, queries, transaction boundaries.

domain
  Entities, value objects, domain services, domain events.

infrastructure
  JPA repositories, RabbitMQ adapters, MinIO adapters, AI clients.
```

Dependency direction:

```text
api -> application -> domain
application -> infrastructure through interfaces where useful
infrastructure -> external systems
```

## 10. Suggested Repository Folder Structure

```text
ai-elearning-saas/
  backend/
    build.gradle or pom.xml
    src/
      main/
        java/
          com/example/learning/
            LearningApplication.java
            auth/
            subjects/
            materials/
            content/
            rag/
            chat/
            assessments/
            knowledge/
            analytics/
            recommendations/
            processing/
            storage/
            ai/
            common/
        resources/
          application.yml
          db/
            migration/
      test/
        java/

  frontend/
    package.json
    next.config.ts
    tsconfig.json
    app/
    features/
    components/
    lib/
    types/

  infra/
    docker-compose.yml
    minio/
    postgres/
    rabbitmq/
    redis/

  docs/
    architecture-foundation.md
    api-contracts.md
    database-model.md
    rag-design.md
    mvp-roadmap.md
```

## 11. Suggested API Surface For MVP

Subject APIs:

```text
POST   /api/subjects
GET    /api/subjects
GET    /api/subjects/{subjectId}
PATCH  /api/subjects/{subjectId}
DELETE /api/subjects/{subjectId}
```

Material APIs:

```text
POST   /api/subjects/{subjectId}/materials
GET    /api/subjects/{subjectId}/materials
GET    /api/materials/{materialId}
GET    /api/materials/{materialId}/status
DELETE /api/materials/{materialId}
```

Chat APIs:

```text
POST   /api/subjects/{subjectId}/conversations
GET    /api/subjects/{subjectId}/conversations
GET    /api/conversations/{conversationId}/messages
POST   /api/conversations/{conversationId}/messages
```

Assessment APIs:

```text
POST   /api/subjects/{subjectId}/tests/generate
GET    /api/subjects/{subjectId}/tests
GET    /api/tests/{testId}
POST   /api/tests/{testId}/attempts
POST   /api/test-attempts/{attemptId}/submit
GET    /api/test-attempts/{attemptId}/results
```

Analytics APIs:

```text
GET    /api/subjects/{subjectId}/knowledge
GET    /api/subjects/{subjectId}/analytics
GET    /api/dashboard/gpa
GET    /api/dashboard/recommendations
```

## 12. Knowledge Model MVP

Use a simple, explainable model first.

Per topic:

```text
mastery_score: 0.0 to 1.0
confidence_score: 0.0 to 1.0
forgetting_risk: 0.0 to 1.0
last_practiced_at
next_review_at
```

Inputs:

- Correct test answers increase mastery.
- Incorrect answers decrease mastery or confidence.
- Repeated mistakes on the same topic increase weak-topic priority.
- Time since last practice increases forgetting risk.
- AI chat can add low-confidence signals only when the user actively solves or explains something.

Initial scoring rule:

```text
topic_score = weighted average of recent test performance, historical performance, and recency.
```

Avoid pretending the model is more precise than it is. MVP should show clear signals:

- Strong
- Medium
- Weak
- Due for review

Percent scores can be displayed, but the product should explain them through visible events like tests, mistakes, and reviews.

## 13. Mini-Test Generation Flow

```text
User clicks Generate Mini-Test
  |
  v
Backend loads knowledge states
  |
  v
Select target topics
  - weak topics
  - topics due for review
  - recently studied topics
  |
  v
Retrieve relevant chunks
  |
  v
Call LLM to generate structured questions
  |
  v
Validate question JSON
  |
  v
Store test and questions
  |
  v
Return test to user
```

Question types for MVP:

- Multiple choice.
- Short answer.
- True/false.

Avoid open-ended essay grading in MVP unless required, because it increases evaluation complexity.

## 14. Deployment Model For MVP

Local development:

```text
docker-compose
  postgres with pgvector
  redis
  rabbitmq
  minio

backend Spring Boot app
frontend Next.js app
```

Production MVP:

```text
1 backend deployable
1 frontend deployable
managed PostgreSQL with pgvector support or self-hosted PostgreSQL
managed Redis or containerized Redis
RabbitMQ
S3-compatible storage
```

Scale path:

1. Keep modular monolith.
2. Split async workers only if processing load requires it.
3. Split AI gateway only if provider complexity, rate limits, or cost controls require it.
4. Split services only after module boundaries and traffic patterns are proven.

