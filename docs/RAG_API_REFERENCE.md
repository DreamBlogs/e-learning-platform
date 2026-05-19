# RAG API Reference

## Semantic Search Endpoint

### Search for relevant content within a subject

**Endpoint**: `POST /api/subjects/{subjectId}/search`

**Description**: Performs semantic similarity search across all materials within a subject. Returns the most relevant text chunks based on the query.

**Path Parameters**:
- `subjectId` (UUID, required): The subject to search within

**Request Body**:
```json
{
  "query": "string (required, not blank)",
  "limit": "integer (optional, 1-50, default: 10)"
}
```

**Response**: `200 OK`
```json
[
  {
    "chunkId": "uuid",
    "materialId": "uuid",
    "chunkIndex": 0,
    "textContent": "The actual chunk text content...",
    "similarityScore": 0.87
  }
]
```

**Response Fields**:
- `chunkId`: Unique identifier for the chunk
- `materialId`: ID of the source material (PDF)
- `chunkIndex`: Position of chunk in the original document (0-based)
- `textContent`: The text content of the chunk
- `similarityScore`: Similarity score (0-1, higher is more relevant)

**Validation Errors**: `400 Bad Request`
```json
{
  "message": "Validation failed",
  "errors": [
    "Query is required",
    "Limit must be between 1 and 50"
  ]
}
```

**Example Request**:
```bash
curl -X POST http://localhost:8080/api/subjects/123e4567-e89b-12d3-a456-426614174000/search \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "query": "What is machine learning?",
    "limit": 5
  }'
```

**Example Response**:
```json
[
  {
    "chunkId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "materialId": "f1e2d3c4-b5a6-7890-1234-567890abcdef",
    "chunkIndex": 0,
    "textContent": "Machine learning is a subset of artificial intelligence that focuses on the development of algorithms and statistical models that enable computer systems to improve their performance on a specific task through experience.",
    "similarityScore": 0.89
  },
  {
    "chunkId": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
    "materialId": "f1e2d3c4-b5a6-7890-1234-567890abcdef",
    "chunkIndex": 3,
    "textContent": "Deep learning is a specialized subset of machine learning that uses neural networks with multiple layers to progressively extract higher-level features from raw input.",
    "similarityScore": 0.76
  }
]
```

## Search Behavior

### Scope
- Search is **scoped by subject**: only materials within the specified subject are searched
- Users can only search subjects they have access to (enforced by authentication)

### Ranking
- Results are ordered by **similarity score** (highest first)
- Similarity score ranges from 0 (not similar) to 1 (identical)
- Typical relevant results have scores > 0.5

### Chunking
- Materials are automatically chunked during ingestion
- Chunk size: ~500-1000 tokens
- Chunks have 100-token overlap for better boundary coverage
- `chunkIndex` preserves original document order

### Performance
- Uses pgvector IVFFlat index for fast similarity search
- Typical query time: < 100ms for subjects with < 1000 chunks
- Embedding generation happens during ingestion (not at query time)

## Integration Notes

### For AI Chat
To use search results in a chat context:

1. Call the search endpoint with the user's question
2. Take top N results (e.g., limit=5)
3. Concatenate `textContent` from results as context
4. Pass context + question to LLM
5. Include `materialId` in response for source attribution

### For Study Recommendations
To find related materials:

1. Extract key concepts from current material
2. Search with those concepts
3. Group results by `materialId`
4. Recommend materials with high aggregate scores

### For Quiz Generation
To generate contextual questions:

1. Search for specific topics
2. Use high-scoring chunks as source material
3. Generate questions based on chunk content
4. Store `chunkId` for answer verification

## Error Handling

### Empty Results
If no chunks match the query, returns empty array `[]`

### Invalid Subject
If subject doesn't exist or user lacks access:
```json
{
  "status": 404,
  "message": "Subject not found"
}
```

### No Materials in Subject
If subject has no processed materials, returns empty array `[]`

## Future Enhancements

### Planned (Not Yet Implemented)
- Filtering by material type
- Date range filtering
- Metadata filtering (tags, categories)
- Hybrid search (keyword + semantic)
- Re-ranking with cross-encoder
- Highlighting matched text
- Pagination for large result sets

### Embedding Provider
Currently uses stub implementation. To integrate real embeddings:

1. Implement `EmbeddingProvider` interface
2. Configure provider in `application.properties`:
   ```properties
   embedding.provider=openai
   openai.api.key=your-key
   ```
3. Restart application

Supported providers (when implemented):
- OpenAI (text-embedding-3-small, text-embedding-3-large)
- Cohere (embed-english-v3.0)
- Local models (sentence-transformers)

## Monitoring

### Metrics to Track
- Search query latency
- Embedding generation time during ingestion
- Chunk count per material
- Average similarity scores
- Cache hit rates

### Logs
Search queries are logged with:
- `subjectId`
- Query text (first 100 chars)
- Result count
- Execution time

Example log:
```
INFO  SemanticSearchService - Performing semantic search. subjectId=123..., query=What is..., limit=10
INFO  SemanticSearchService - Search completed. subjectId=123..., resultsCount=5
```
