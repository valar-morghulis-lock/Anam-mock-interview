ALTER TABLE interview_session DROP CONSTRAINT interview_session_persona_style_check;
ALTER TABLE interview_session ADD CONSTRAINT interview_session_persona_style_check
    CHECK (persona_style IN ('SUPPORTIVE', 'BAR_RAISER'));

ALTER TABLE interview_session DROP CONSTRAINT interview_session_seniority_check;
ALTER TABLE interview_session ADD CONSTRAINT interview_session_seniority_check
    CHECK (seniority IN ('JUNIOR', 'MID', 'SENIOR', 'STAFF'));

ALTER TABLE interview_session DROP CONSTRAINT interview_session_status_check;
ALTER TABLE interview_session ADD CONSTRAINT interview_session_status_check
    CHECK (status IN ('CREATED', 'IN_PROGRESS', 'COMPLETED', 'ABANDONED'));

ALTER TABLE transcript_message DROP CONSTRAINT transcript_message_speaker_check;
ALTER TABLE transcript_message ADD CONSTRAINT transcript_message_speaker_check
    CHECK (speaker IN ('INTERVIEWER', 'CANDIDATE'));