CREATE TABLE session_competency (
    session_id    uuid NOT NULL REFERENCES interview_session(id) ON DELETE CASCADE,
    competency_id uuid NOT NULL REFERENCES competency(id),
    PRIMARY KEY (session_id, competency_id)
);