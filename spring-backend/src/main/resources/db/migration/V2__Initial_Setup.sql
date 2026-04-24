
DROP VIEW IF EXISTS v_today_glucose_records CASCADE;
DROP VIEW IF EXISTS v_active_glucose_records CASCADE;
DROP VIEW IF EXISTS your_glucose_view_name CASCADE;

ALTER TABLE glucose_records ALTER COLUMN id SET DATA TYPE bigint;

CREATE VIEW v_active_glucose_records AS
    SELECT *
    FROM glucose_records
    WHERE is_active = true;

CREATE VIEW v_today_glucose_records AS
    SELECT *
    FROM v_active_glucose_records
    WHERE DATE(recorded_at) = CURRENT_DATE;