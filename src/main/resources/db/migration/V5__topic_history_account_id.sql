-- Dono explícito do histórico: evita join obrigatória com topic para saber a conta.
ALTER TABLE topic_history
    ADD COLUMN account_id BIGINT;

UPDATE topic_history th
SET account_id = t.account_id
FROM topic t
WHERE th.topic_id = t.id;

ALTER TABLE topic_history
    ALTER COLUMN account_id SET NOT NULL;

ALTER TABLE topic_history
    ADD CONSTRAINT fk_topic_history_account FOREIGN KEY (account_id) REFERENCES account (id);

CREATE INDEX IF NOT EXISTS idx_topic_history_account_id ON topic_history (account_id);
