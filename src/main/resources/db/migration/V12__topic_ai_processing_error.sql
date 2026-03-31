CREATE TABLE IF NOT EXISTS topic_ai_processing_error (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    problematic_text VARCHAR(4000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_ai_processing_error_topic
        FOREIGN KEY (topic_id)
            REFERENCES topic (id)
);
