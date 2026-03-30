CREATE TABLE IF NOT EXISTS topic_history
(
    id         BIGSERIAL PRIMARY KEY,
    topic_id   BIGINT                   NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status     VARCHAR(32)              NOT NULL,
    CONSTRAINT fk_topic_history_topic FOREIGN KEY (topic_id) REFERENCES topic (id)
);

CREATE INDEX IF NOT EXISTS idx_topic_history_topic_id ON topic_history (topic_id);
