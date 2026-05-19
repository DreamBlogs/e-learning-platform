CREATE TABLE content_chunks (
    id UUID PRIMARY KEY,
    material_id UUID NOT NULL REFERENCES materials(id) ON DELETE CASCADE,
    chunk_index INTEGER NOT NULL,
    text_content TEXT NOT NULL,
    token_count INTEGER NOT NULL,
    embedding vector(1536),
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE(material_id, chunk_index)
);

CREATE INDEX idx_content_chunks_material_id ON content_chunks(material_id);
CREATE INDEX idx_content_chunks_embedding ON content_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

COMMENT ON TABLE content_chunks IS 'Stores text chunks and their embeddings for semantic search';
COMMENT ON COLUMN content_chunks.chunk_index IS 'Sequential index to preserve chunk order within a material';
COMMENT ON COLUMN content_chunks.token_count IS 'Approximate token count for the chunk';
COMMENT ON COLUMN content_chunks.embedding IS 'Vector embedding (1536 dimensions for OpenAI text-embedding-3-small)';
