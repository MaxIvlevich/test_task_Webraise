CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users
(
    id         UUID PRIMARY KEY    DEFAULT uuid_generate_v4(),
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name  VARCHAR(50),
    created_time TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);