Now implement the first version of the material ingestion pipeline.

Requirements:

* multipart file upload
* MinIO integration
* material lifecycle statuses
* RabbitMQ event publishing
* async consumer
* PDF text extraction only
* store extracted text in database
* proper error handling
* logging
* retry-safe processing

Flow:
upload file
→ save file to MinIO
→ create material row
→ publish MATERIAL_PROCESS_REQUESTED
→ async consumer processes material
→ extract PDF text
→ save extracted text
→ update material status

Do NOT implement:

* embeddings
* AI chat
* OCR
* DOCX/PPTX parsing
* recommendations
* quizzes

Goal:
Create a stable ingestion foundation for future RAG pipeline.
