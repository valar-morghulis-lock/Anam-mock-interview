ALTER TABLE answer_feedback DROP CONSTRAINT answer_feedback_score_check;
ALTER TABLE answer_feedback ADD CONSTRAINT answer_feedback_score_check
    CHECK (score BETWEEN 0 AND 5);