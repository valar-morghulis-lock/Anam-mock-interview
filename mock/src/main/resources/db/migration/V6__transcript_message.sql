CREATE TABLE transcript_message (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id          uuid NOT NULL REFERENCES interview_session(id) ON DELETE CASCADE,
    session_question_id uuid REFERENCES session_question(id),
    speaker             varchar(15) NOT NULL
        CHECK (speaker IN ('interviewer', 'candidate')),
    content             text NOT NULL,
    spoken_at           timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_transcript_session ON transcript_message(session_id);
CREATE INDEX idx_transcript_session_question ON transcript_message(session_question_id);