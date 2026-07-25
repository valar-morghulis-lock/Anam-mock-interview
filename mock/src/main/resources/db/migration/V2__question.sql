CREATE TABLE question (
    id            uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    text          text NOT NULL,
    competency_id uuid NOT NULL REFERENCES competency(id)
);

CREATE INDEX idx_question_competency ON question(competency_id);