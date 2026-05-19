# Material Ingestion Pipeline

Current scope:

- multipart PDF upload
- MinIO object storage
- material lifecycle status updates
- RabbitMQ processing event
- async consumer
- PDF text extraction with PDFBox
- extracted text persistence

Not included yet:

- embeddings
- chunking
- RAG
- OCR
- DOCX/PPTX parsing
- quizzes
- recommendations

## Flow

```text
POST /api/subjects/{subjectId}/materials/upload
  |
  v
Validate PDF
  |
  v
Upload original file to MinIO
  |
  v
Create materials row with UPLOADED status
  |
  v
After DB commit, publish MaterialProcessingRequestedEvent to material.processing
  |
  v
RabbitMQ consumer receives event
  |
  v
Mark material PROCESSING
  |
  v
Download original PDF from MinIO
  |
  v
Extract text with PDFBox
  |
  v
Upsert material_extracted_texts row
  |
  v
Mark material PROCESSED
```

If processing fails, the material is marked `FAILED` with `error_message`.

## Key Files

- Upload API: `backend/src/main/java/com/example/learning/materials/api/MaterialController.java`
- Upload service: `backend/src/main/java/com/example/learning/materials/application/MaterialService.java`
- Consumer: `backend/src/main/java/com/example/learning/materials/infrastructure/MaterialProcessingConsumer.java`
- Processor: `backend/src/main/java/com/example/learning/materials/application/MaterialIngestionProcessor.java`
- PDF extraction: `backend/src/main/java/com/example/learning/materials/application/PdfTextExtractionService.java`
- Extracted text entity: `backend/src/main/java/com/example/learning/materials/domain/MaterialExtractedText.java`
- Migration: `backend/src/main/resources/db/migration/V2__material_extracted_texts.sql`

## Smoke Test

Upload a PDF:

```bash
curl -s -X POST "$BASE_URL/api/subjects/$SUBJECT_ID/materials/upload" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/absolute/path/to/sample.pdf;type=application/pdf"
```

Check status:

```bash
curl -s "$BASE_URL/api/materials/$MATERIAL_ID" \
  -H "Authorization: Bearer $TOKEN" | jq '.data.status, .data.errorMessage'
```

Read extracted text:

```bash
curl -s "$BASE_URL/api/materials/$MATERIAL_ID/extracted-text" \
  -H "Authorization: Bearer $TOKEN" | jq
```

