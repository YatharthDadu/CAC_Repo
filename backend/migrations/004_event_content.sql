-- 004: store everything the app needs to render an honest event page.
-- Before this, the Android client had to guess descriptions and categories from the title.

ALTER TABLE events ADD COLUMN IF NOT EXISTS description TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS category TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS source_url TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS source_platform TEXT NOT NULL DEFAULT '';
ALTER TABLE events ADD COLUMN IF NOT EXISTS duration_hours NUMERIC(4,1) NOT NULL DEFAULT 3.0
    CHECK (duration_hours > 0 AND duration_hours <= 24);
-- TRUE when the source gave no usable date and the pipeline filled in a placeholder.
ALTER TABLE events ADD COLUMN IF NOT EXISTS is_estimated_date BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS events_event_date_idx ON events (event_date);
