CREATE TABLE session_question (
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id  uuid NOT NULL REFERENCES interview_session(id) ON DELETE CASCADE,
    question_id uuid NOT NULL REFERENCES question(id),
    sequence_no int NOT NULL,
    skipped     boolean NOT NULL DEFAULT false,
    asked_at    timestamptz,
    UNIQUE (session_id, sequence_no)
);

CREATE INDEX idx_session_question_session ON session_question(session_id);