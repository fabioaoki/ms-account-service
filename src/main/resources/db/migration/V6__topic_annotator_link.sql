CREATE TABLE IF NOT EXISTS topic_annotator_link
(
    id                      BIGSERIAL PRIMARY KEY,
    topic_id                BIGINT      NOT NULL UNIQUE,
    topic_owner_account_id  BIGINT      NOT NULL,
    annotator_account_id    BIGINT      NOT NULL,
    resume                  TEXT        NOT NULL,
    created_at              TIMESTAMP   NOT NULL,
    last_updated_at         TIMESTAMP,
    CONSTRAINT fk_tal_topic FOREIGN KEY (topic_id) REFERENCES topic (id),
    CONSTRAINT fk_tal_owner FOREIGN KEY (topic_owner_account_id) REFERENCES account (id),
    CONSTRAINT fk_tal_annotator FOREIGN KEY (annotator_account_id) REFERENCES account (id)
);

CREATE INDEX IF NOT EXISTS idx_tal_topic_owner ON topic_annotator_link (topic_owner_account_id);
CREATE INDEX IF NOT EXISTS idx_tal_annotator ON topic_annotator_link (annotator_account_id);

CREATE TABLE IF NOT EXISTS topic_annotator_link_history
(
    id                   BIGSERIAL PRIMARY KEY,
    link_id              BIGINT    NOT NULL,
    annotator_account_id BIGINT    NOT NULL,
    resume               TEXT      NOT NULL,
    created_at           TIMESTAMP NOT NULL,
    CONSTRAINT fk_talh_link FOREIGN KEY (link_id) REFERENCES topic_annotator_link (id),
    CONSTRAINT fk_talh_annotator FOREIGN KEY (annotator_account_id) REFERENCES account (id)
);

CREATE INDEX IF NOT EXISTS idx_talh_link ON topic_annotator_link_history (link_id);
CREATE INDEX IF NOT EXISTS idx_talh_annotator ON topic_annotator_link_history (annotator_account_id);
