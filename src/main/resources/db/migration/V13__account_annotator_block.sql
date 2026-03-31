CREATE TABLE IF NOT EXISTS account_annotator_block (
    id BIGSERIAL PRIMARY KEY,
    blocker_account_id BIGINT NOT NULL,
    blocked_account_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_annotator_block_blocker
        FOREIGN KEY (blocker_account_id)
            REFERENCES account (id),
    CONSTRAINT fk_account_annotator_block_blocked
        FOREIGN KEY (blocked_account_id)
            REFERENCES account (id),
    CONSTRAINT uk_account_annotator_block_blocker_blocked
        UNIQUE (blocker_account_id, blocked_account_id)
);
