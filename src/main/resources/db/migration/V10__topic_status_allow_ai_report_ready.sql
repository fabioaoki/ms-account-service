-- Alinha topic.status com TopicStatusEnum (incl. AI_REPORT_READY após consolidação IA).
-- Hibernate / ddl-auto pode não atualizar CHECK existente no PostgreSQL.

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
                 'AI_REPORT_READY'
                 )
        );
