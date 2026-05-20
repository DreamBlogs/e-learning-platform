CREATE TABLE material_extracted_texts (
    id UUID PRIMARY KEY,
    material_id UUID NOT NULL UNIQUE REFERENCES materials(id) ON DELETE CASCADE,
    text_content TEXT NOT NULL,
    page_count INTEGER,
    character_count BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
