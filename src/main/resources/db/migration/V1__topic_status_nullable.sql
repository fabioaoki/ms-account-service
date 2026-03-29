-- topic.status must allow NULL (ANNOTATOR rows). Hibernate ddl-auto may not alter nullability on existing DBs.
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
                          AND column_name = 'status'
                          AND is_nullable = 'NO') THEN
            ALTER TABLE topic
                ALTER COLUMN status DROP NOT NULL;
        END IF;
    END
$$;
