CREATE TABLE subscriptions
(
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_name VARCHAR(50) NOT NULL,
    start_date   DATE        NOT NULL,
    end_date     DATE,
    user_id      UUID        NOT NULL,
    created_time   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_time   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_subscriptions_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);
CREATE INDEX idx_subscriptions_service_name ON subscriptions (service_name);

