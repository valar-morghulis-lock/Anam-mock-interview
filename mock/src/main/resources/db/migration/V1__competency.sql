CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE competency (
    id   uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(50) NOT NULL UNIQUE
);