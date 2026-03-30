ALTER TABLE topic_annotator_link
    DROP CONSTRAINT IF EXISTS topic_annotator_link_topic_id_key;

ALTER TABLE topic_annotator_link
    ALTER COLUMN resume DROP NOT NULL;

ALTER TABLE topic_annotator_link_history
    ALTER COLUMN resume DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_tal_topic_annotator ON topic_annotator_link (topic_id, annotator_account_id);
