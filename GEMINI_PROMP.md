I need you to perform a FULL end-to-end verification of my AI E-Learning backend ingestion pipeline.

The project is a Java Spring Boot modular monolith with:

* PostgreSQL + pgvector
* Redis
* RabbitMQ
* MinIO
* Flyway
* JWT authentication
* Swagger/OpenAPI

The backend already starts successfully and Swagger endpoints are visible.

Your task is NOT to implement new features.
Your task is to VERIFY that the current ingestion pipeline works correctly and identify all architectural, runtime, logic, async, storage, security, and persistence problems.

You must act like a senior backend engineer performing a deep smoke test and infrastructure validation.

==================================================
PROJECT GOAL
============

The ingestion pipeline should work like this:

1. User uploads PDF file
2. File is stored in MinIO
3. Material row is created in PostgreSQL
4. RabbitMQ event is published
5. Async consumer receives event
6. PDF text extraction starts
7. Extracted text is stored in database
8. Material status updates correctly

==================================================
YOUR TASKS
==========

Perform a COMPLETE verification of the current implementation.

Check EVERYTHING below carefully.

==================================================

1. APPLICATION STARTUP VALIDATION
   ==================================================

Verify:

* Spring Boot starts correctly
* No critical startup exceptions
* Flyway migrations run correctly
* PostgreSQL connection works
* RabbitMQ connection works
* Redis connection works
* MinIO connection works
* Swagger/OpenAPI loads correctly
* No circular dependency problems
* No bean initialization failures
* No invalid configuration values

Check:

* application.yml
* environment variables
* docker-compose.yml
* startup logs

==================================================
2. DOCKER INFRASTRUCTURE VALIDATION
===================================

Verify Docker Compose services:

* postgres
* redis
* rabbitmq
* minio

Check:

* all containers healthy
* exposed ports correct
* credentials configured correctly
* service names consistent
* persistent volumes configured
* pgvector extension enabled
* MinIO bucket configuration valid

Verify:

* RabbitMQ management UI
* MinIO UI
* PostgreSQL accessibility

==================================================
3. SECURITY VALIDATION
======================

Verify:

* protected routes return 401 without JWT
* public routes accessible
* JWT generation works
* JWT validation works
* password hashing works
* authentication flow works
* invalid token handling works
* security filters ordered correctly
* Swagger access configuration valid

Test:

* register
* login
* protected subject routes

Look for:

* security bypasses
* missing auth checks
* broken role logic
* insecure endpoints

==================================================
4. SUBJECT CRUD VALIDATION
==========================

Verify FULL CRUD flow:

* create subject
* get subject
* list subjects
* update subject
* delete subject

Check:

* validation
* database persistence
* ownership boundaries
* response structure
* DTO mapping
* error handling
* timestamps
* UUID handling

Verify:

* invalid IDs
* malformed payloads
* unauthorized access
* missing entities

==================================================
5. MATERIAL INGESTION PIPELINE VALIDATION
=========================================

This is the MOST IMPORTANT part.

Verify the complete flow:

UPLOAD
→ MinIO storage
→ DB persistence
→ RabbitMQ event
→ async consumer
→ PDF extraction
→ extracted text persistence
→ status updates

==================================================
6. FILE UPLOAD VALIDATION
=========================

Verify:

* multipart upload works
* file validation works
* supported file types enforced
* invalid file handling
* empty file handling
* large file handling
* content-type validation
* upload endpoint stability

Check:

* generated object keys
* duplicate uploads
* temporary file cleanup
* upload memory safety

==================================================
7. MINIO STORAGE VALIDATION
===========================

Verify:

* file actually stored in MinIO
* bucket exists
* object key matches database
* object retrieval works
* metadata stored correctly

Check:

* upload failures
* bucket missing scenarios
* invalid credentials handling
* retry behavior

==================================================
8. DATABASE VALIDATION
======================

Verify:

* material row creation
* correct statuses
* extracted text persistence
* transaction consistency
* timestamps
* foreign keys
* indexes
* UUID consistency

Check:

* rollback behavior
* orphaned records
* invalid relations
* nullable field problems

==================================================
9. RABBITMQ VALIDATION
======================

Verify:

* event publishing works
* queue exists
* consumer receives messages
* messages acknowledged correctly
* retry-safe processing
* dead-letter handling
* queue durability

Check:

* duplicate processing
* consumer crash behavior
* poison messages
* message serialization

==================================================
10. ASYNC CONSUMER VALIDATION
=============================

Verify:

* consumer triggers correctly
* material loads correctly
* extraction pipeline executes
* status transitions valid

Check:

* idempotency
* concurrency issues
* duplicate execution
* race conditions
* transaction boundaries
* retry behavior

==================================================
11. PDF EXTRACTION VALIDATION
=============================

Verify:

* PDF text extraction actually works
* extracted text readable
* extraction errors handled
* malformed PDFs handled

Test:

* normal PDF
* empty PDF
* corrupted PDF
* scanned PDF
* large PDF

Check:

* memory usage
* extraction failures
* timeout risks

==================================================
12. MATERIAL STATUS LIFECYCLE VALIDATION
========================================

Verify valid lifecycle:

UPLOADED
→ PROCESSING
→ PROCESSED

or

UPLOADED
→ PROCESSING
→ FAILED

Check:

* invalid transitions
* stuck statuses
* retry behavior
* failed recovery

==================================================
13. LOGGING VALIDATION
======================

Verify logs are useful.

Check:

* upload logs
* processing logs
* consumer logs
* error logs
* correlation IDs if present

Look for:

* missing context
* swallowed exceptions
* unreadable logs
* security leaks in logs

==================================================
14. FAILURE SCENARIOS
=====================

Test and verify:

* RabbitMQ down
* PostgreSQL down
* MinIO unavailable
* invalid PDFs
* duplicate uploads
* extraction exceptions
* consumer crashes

Verify:

* graceful failure handling
* retries
* failed statuses
* no corrupted data

==================================================
15. ARCHITECTURE REVIEW
=======================

Review architecture quality.

Identify:

* overengineering
* premature abstractions
* bad module boundaries
* incorrect layering
* dependency problems
* anti-patterns
* scalability risks

Check:

* modular monolith quality
* service boundaries
* async design
* transaction design

==================================================
16. CODE QUALITY REVIEW
=======================

Review:

* DTO usage
* entity design
* service responsibilities
* repository usage
* exception handling
* naming consistency
* package structure

Identify:

* duplicated logic
* God services
* bad abstractions
* leaking infrastructure logic
* security risks

==================================================
17. OUTPUT FORMAT
=================

Return results in this format:

1. WORKING FEATURES
2. BROKEN FEATURES
3. CRITICAL ISSUES
4. ARCHITECTURE RISKS
5. SECURITY RISKS
6. PERFORMANCE RISKS
7. BUGS FOUND
8. IMPROVEMENT SUGGESTIONS
9. PRIORITY FIXES
10. READY FOR NEXT STAGE? (YES/NO)

For every issue:

* explain the problem
* explain why it matters
* explain how to fix it

IMPORTANT:
Do NOT suggest adding new product features.
Do NOT generate random architecture.
Focus ONLY on verifying and stabilizing the ingestion foundation.
