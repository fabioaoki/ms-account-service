-- Remove substituta experimental se existir (undo local).
DROP TABLE IF EXISTS ia_response;

ALTER TABLE topic_ai_report
    ADD COLUMN IF NOT EXISTS last_updated_at TIMESTAMP;

UPDATE topic_ai_report
SET last_updated_at = created_at
WHERE last_updated_at IS NULL;

ALTER TABLE topic_ai_report
    ALTER COLUMN last_updated_at SET NOT NULL;
