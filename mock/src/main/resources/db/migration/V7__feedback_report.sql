CREATE TABLE feedback_report (
    id                 uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id         uuid NOT NULL UNIQUE REFERENCES interview_session(id) ON DELETE CASCADE,
    overall_strengths  text,
    created_at         timestamptz NOT NULL DEFAULT now()
);