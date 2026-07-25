CREATE TABLE interview_session (
    id             uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    role           varchar(100) NOT NULL,
    seniority      varchar(20) NOT NULL
        CHECK (seniority IN ('junior', 'mid', 'senior', 'staff')),
    persona_style  varchar(30) NOT NULL
        CHECK (persona_style IN ('supportive', 'bar_raiser')),
    status         varchar(20) NOT NULL DEFAULT 'created'
        CHECK (status IN ('created', 'in_progress', 'completed', 'abandoned')),
    time_limit_sec int NOT NULL,
    started_at     timestamptz,
    ended_at       timestamptz,
    created_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_session_status ON interview_session(status);