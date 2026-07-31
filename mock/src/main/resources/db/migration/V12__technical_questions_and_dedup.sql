CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE question ADD COLUMN role_tag VARCHAR(100);
ALTER TABLE question ADD COLUMN language_tag VARCHAR(50);
ALTER TABLE question ADD COLUMN source VARCHAR(20) NOT NULL DEFAULT 'SEEDED'
    CHECK (source IN ('SEEDED', 'GENERATED'));
ALTER TABLE question ADD COLUMN occurrence_count INT NOT NULL DEFAULT 0;

-- Trigram index for fast fuzzy-similarity lookups when checking for duplicates
CREATE INDEX idx_question_text_trgm ON question USING gin (text gin_trgm_ops);

INSERT INTO competency (name) VALUES ('technical');