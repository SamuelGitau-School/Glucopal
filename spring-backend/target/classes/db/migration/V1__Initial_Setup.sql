CREATE TABLE IF NOT EXISTS users (
  id              BIGINT       PRIMARY KEY,
  email           VARCHAR(255)  NOT NULL UNIQUE,
  display_name    VARCHAR(100),
  three_word_tag  VARCHAR(60)   CHECK (
                    three_word_tag IS NULL OR
                    three_word_tag ~ '^[a-zA-Z]+-[a-zA-Z]+-[a-zA-Z]+$'
                  ),
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS glucose_records (
  id              BIGINT        PRIMARY KEY,
  user_id         INT           NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  value           NUMERIC(6,2)  NOT NULL CHECK (value > 0),
  meal_context    VARCHAR(10)   NOT NULL DEFAULT 'fasting'
                                CHECK (meal_context IN ('fasting','before','after')),
  note            TEXT,
  status_label    VARCHAR(20)   CHECK (
                    status_label IS NULL OR
                    status_label IN ('low','normal','high','very_high')
                  ),
  three_word_tag  VARCHAR(60)   CHECK (
                    three_word_tag IS NULL OR
                    three_word_tag ~ '^[a-zA-Z]+-[a-zA-Z]+-[a-zA-Z]+$'
                  ),
  recorded_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  is_deleted      BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_glucose_user        ON glucose_records(user_id);
CREATE INDEX IF NOT EXISTS idx_glucose_recorded    ON glucose_records(recorded_at DESC);
CREATE INDEX IF NOT EXISTS idx_glucose_user_date   ON glucose_records(user_id, recorded_at DESC);


CREATE TABLE IF NOT EXISTS glucose_record_audit_log (
  id          SERIAL        PRIMARY KEY,
  record_id   INT           NOT NULL REFERENCES glucose_records(id) ON DELETE CASCADE,
  operation   VARCHAR(10)   NOT NULL CHECK (operation IN ('INSERT','UPDATE','DELETE')),
  field_name  VARCHAR(60),
  old_value   TEXT,
  new_value   TEXT,
  changed_by  VARCHAR(255)  NOT NULL DEFAULT CURRENT_USER,
  changed_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_glucose_audit_record  ON glucose_record_audit_log(record_id);
CREATE INDEX IF NOT EXISTS idx_glucose_audit_changed ON glucose_record_audit_log(changed_at DESC);



CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_users_updated_at
  BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_glucose_updated_at
  BEFORE UPDATE ON glucose_records
  FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();


CREATE OR REPLACE FUNCTION fn_audit_glucose_record()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN

  IF TG_OP = 'INSERT' THEN
    INSERT INTO glucose_record_audit_log
      (record_id, operation, field_name, old_value, new_value)
    VALUES
      (NEW.id, 'INSERT', NULL, NULL, NULL);


  ELSIF TG_OP = 'UPDATE' THEN

    IF OLD.is_deleted = FALSE AND NEW.is_deleted = TRUE THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'DELETE', 'is_deleted', 'false', 'true');
    END IF;


    IF OLD.value IS DISTINCT FROM NEW.value THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'UPDATE', 'value', OLD.value::TEXT, NEW.value::TEXT);
    END IF;

    IF OLD.meal_context IS DISTINCT FROM NEW.meal_context THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'UPDATE', 'meal_context', OLD.meal_context, NEW.meal_context);
    END IF;

    IF OLD.note IS DISTINCT FROM NEW.note THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'UPDATE', 'note', OLD.note, NEW.note);
    END IF;

    IF OLD.status_label IS DISTINCT FROM NEW.status_label THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'UPDATE', 'status_label', OLD.status_label, NEW.status_label);
    END IF;

    IF OLD.three_word_tag IS DISTINCT FROM NEW.three_word_tag THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'UPDATE', 'three_word_tag', OLD.three_word_tag, NEW.three_word_tag);
    END IF;

    IF OLD.recorded_at IS DISTINCT FROM NEW.recorded_at THEN
      INSERT INTO glucose_record_audit_log
        (record_id, operation, field_name, old_value, new_value)
      VALUES
        (NEW.id, 'UPDATE', 'recorded_at', OLD.recorded_at::TEXT, NEW.recorded_at::TEXT);
    END IF;

  END IF;

  RETURN NEW;
END;
$$;

CREATE TRIGGER trg_audit_glucose_record
  AFTER INSERT OR UPDATE ON glucose_records
  FOR EACH ROW EXECUTE FUNCTION fn_audit_glucose_record();



CREATE OR REPLACE VIEW v_active_glucose_records AS
  SELECT
    gr.id,
    gr.user_id,
    gr.value,
    gr.meal_context,
    gr.note,
    gr.status_label,
    gr.three_word_tag,
    gr.recorded_at,
    gr.created_at,
    u.display_name AS user_display_name
  FROM glucose_records gr
  JOIN users u ON u.id = gr.user_id
  WHERE gr.is_deleted = FALSE
    AND u.is_deleted = FALSE
  ORDER BY gr.recorded_at DESC;

CREATE OR REPLACE VIEW v_today_glucose_records AS
  SELECT *
  FROM v_active_glucose_records
  WHERE recorded_at >= CURRENT_DATE
    AND recorded_at < CURRENT_DATE + INTERVAL '1 day';

-- Daily averages per user — feeds glucose.avgToday and glucose.avgWeek stats
CREATE OR REPLACE VIEW v_glucose_daily_avg AS
  SELECT
    user_id,
    DATE(recorded_at AT TIME ZONE 'UTC') AS reading_date,
    ROUND(AVG(value), 1)                  AS avg_value,
    COUNT(*)                              AS reading_count
  FROM glucose_records
  WHERE is_deleted = FALSE
  GROUP BY user_id, reading_date
  ORDER BY reading_date DESC;

CREATE OR REPLACE VIEW v_glucose_audit AS
  SELECT
    gal.id,
    gal.record_id,
    gr.user_id,
    gal.operation,
    gal.field_name,
    gal.old_value,
    gal.new_value,
    gal.changed_by,
    gal.changed_at
  FROM glucose_record_audit_log gal
  JOIN glucose_records gr ON gr.id = gal.record_id
  ORDER BY gal.changed_at DESC;
