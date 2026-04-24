DROP VIEW IF EXISTS v_active_glucose_records;

ALTER TABLE glucose_records ALTER COLUMN id SET DATA TYPE bigint;

CREATE VIEW v_active_glucose_records AS
    SELECT *
    FROM glucose_records
    WHERE is_active = true;