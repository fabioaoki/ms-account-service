-- Bases com flyway_schema_history em 14 mas sem esta tabela (ex.: V14 divergente no passado ou só Hibernate).
-- Mantém o mesmo DDL que V14; idempotente com IF NOT EXISTS.
-- Depois: garante summary_text como TEXT (corrige varchar(255) de ddl-auto=update).

CREATE TABLE IF NOT EXISTS account_presentation_summary (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    summary_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated_at TIMESTAMP NULL,
    CONSTRAINT uk_account_presentation_summary_account UNIQUE (account_id),
    CONSTRAINT fk_account_presentation_summary_account
        FOREIGN KEY (account_id) REFERENCES account(id)
);

ALTER TABLE account_presentation_summary
    ALTER COLUMN summary_text TYPE TEXT USING summary_text::TEXT;
