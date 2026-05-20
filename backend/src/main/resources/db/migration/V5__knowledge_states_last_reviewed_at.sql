ALTER TABLE knowledge_states
    ADD COLUMN IF NOT EXISTS last_reviewed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;

UPDATE knowledge_states
SET last_reviewed_at = updated_at
WHERE last_reviewed_at IS NULL;
