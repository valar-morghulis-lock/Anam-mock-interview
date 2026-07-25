INSERT INTO competency (name) VALUES
    ('leadership'), ('conflict'), ('failure'), ('teamwork'), ('delivery');

INSERT INTO question (text, competency_id)
SELECT text, (SELECT id FROM competency WHERE name = competency_name)
FROM (VALUES
    ('Tell me about a time you led a project with unclear requirements.', 'leadership'),
    ('Describe a time you had to motivate a struggling team member.', 'leadership'),
    ('Give an example of when you disagreed with a manager''s decision.', 'conflict'),
    ('Tell me about a conflict with a teammate and how you resolved it.', 'conflict'),
    ('Describe a project that failed. What happened?', 'failure'),
    ('Tell me about a mistake you made and what you learned.', 'failure'),
    ('Give an example of resolving a disagreement within your team.', 'teamwork'),
    ('Describe a time you had to rely on a teammate you didn''t fully trust.', 'teamwork'),
    ('Tell me about a time you missed a deadline. What did you do?', 'delivery'),
    ('Describe shipping a feature under significant time pressure.', 'delivery')
) AS q(text, competency_name);