-- last_updated_at preenchido apenas após o primeiro PUT; na criação fica NULL.
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.tables
                   WHERE table_schema = 'public'
                     AND table_name = 'topic')
            AND EXISTS (SELECT 1
                        FROM information_schema.columns
                        WHERE table_schema = 'public'
                          AND table_name = 'topic'
                          AND column_name = 'last_updated_at'
                          AND is_nullable = 'NO') THEN
            ALTER TABLE topic
                ALTER COLUMN last_updated_at DROP NOT NULL;
        END IF;
    END
$$;
