CREATE TABLE answer_feedback (
    id                   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    session_question_id uuid NOT NULL UNIQUE REFERENCES session_question(id) ON DELETE CASCADE,
    has_situation        boolean NOT NULL DEFAULT false,
    has_task             boolean NOT NULL DEFAULT false,
    has_action           boolean NOT NULL DEFAULT false,
    has_result           boolean NOT NULL DEFAULT false,
    score                int NOT NULL CHECK (score BETWEEN 1 AND 5),
    improvement          text
);