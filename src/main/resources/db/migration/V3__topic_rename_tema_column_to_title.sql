-- Coluna renomeada de tema -> title (alinhado a EntityConstants.COLUMN_TITLE).
DO
$$
    BEGIN
        IF EXISTS (SELECT 1
                   FROM information_schema.columns
                   WHERE table_schema = 'public'
                     AND table_name = 'topic'
                     AND column_name = 'tema')
            AND NOT EXISTS (SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = 'public'
                              AND table_name = 'topic'
                              AND column_name = 'title') THEN
            ALTER TABLE topic
                RENAME COLUMN tema TO title;
        END IF;
    END
$$;
