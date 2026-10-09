-- =====================================================================
-- Migration 002: RSVPs, peer-trending counts, and dashboard goal fields
-- Run AFTER schema.sql. Safe to re-run (idempotent).
-- =====================================================================

-- 1. Dashboard goal fields on users (powers the "14/30 NHS Hours" ring)
ALTER TABLE users ADD COLUMN IF NOT EXISTS target_club TEXT DEFAULT 'National Honor Society';
ALTER TABLE users ADD COLUMN IF NOT EXISTS goal_hours NUMERIC DEFAULT 30;

-- 2. schema.sql had no INSERT policy on users, so the app could never
--    create a profile row. Allow users to insert ONLY their own row.
DROP POLICY IF EXISTS "Users can insert own profile" ON users;
CREATE POLICY "Users can insert own profile"
    ON users FOR INSERT
    WITH CHECK (auth.uid() = id);

-- 3. RSVPs table (one row per user per event)
CREATE TABLE IF NOT EXISTS rsvps (
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    event_id UUID REFERENCES events(id) ON DELETE CASCADE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, event_id)
);

CREATE INDEX IF NOT EXISTS rsvps_event_id_idx ON rsvps (event_id);

ALTER TABLE rsvps ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Users can view own rsvps" ON rsvps;
CREATE POLICY "Users can view own rsvps"
    ON rsvps FOR SELECT
    USING (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can create own rsvps" ON rsvps;
CREATE POLICY "Users can create own rsvps"
    ON rsvps FOR INSERT
    WITH CHECK (auth.uid() = user_id);

DROP POLICY IF EXISTS "Users can delete own rsvps" ON rsvps;
CREATE POLICY "Users can delete own rsvps"
    ON rsvps FOR DELETE
    USING (auth.uid() = user_id);

-- 4. Peer-trending counts.
--    RLS hides other students' RSVP rows, so this SECURITY DEFINER function
--    returns ONLY aggregate counts (never names/IDs) and excludes the caller.
CREATE OR REPLACE FUNCTION get_peer_rsvp_counts(event_ids UUID[])
RETURNS TABLE (event_id UUID, rsvp_count BIGINT)
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT r.event_id, COUNT(*)::BIGINT AS rsvp_count
    FROM rsvps r
    WHERE r.event_id = ANY(event_ids)
      AND r.user_id IS DISTINCT FROM auth.uid()
    GROUP BY r.event_id;
$$;

REVOKE EXECUTE ON FUNCTION get_peer_rsvp_counts(UUID[]) FROM PUBLIC, anon;
GRANT EXECUTE ON FUNCTION get_peer_rsvp_counts(UUID[]) TO authenticated;
