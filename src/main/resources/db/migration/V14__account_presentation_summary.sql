CREATE TABLE account_presentation_summary (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    summary_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated_at TIMESTAMP NULL,
    CONSTRAINT uk_account_presentation_summary_account UNIQUE (account_id),
    CONSTRAINT fk_account_presentation_summary_account
        FOREIGN KEY (account_id) REFERENCES account(id)
);
