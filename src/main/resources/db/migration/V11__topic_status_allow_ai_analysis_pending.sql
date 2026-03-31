ALTER TABLE topic
    DROP CONSTRAINT IF EXISTS topic_status_check;

ALTER TABLE topic
    ADD CONSTRAINT topic_status_check
    CHECK (
            status IS NULL
            OR status IN (
                 'OPEN',
                 'CLOSED',
                 'CANCELED',
                 'REVIEWED',
                 'AI_ANALYSIS_PENDING',
                 'AI_REPORT_READY'
                 )
        );
