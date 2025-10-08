DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_database WHERE datname = 'venues_db'
    ) THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE venues_db');
END IF;

    IF NOT EXISTS (
        SELECT FROM pg_database WHERE datname = 'tickets_db'
    ) THEN
        PERFORM dblink_exec('dbname=postgres', 'CREATE DATABASE tickets_db');
END IF;
END
$$;
