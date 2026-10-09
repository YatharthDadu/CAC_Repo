-- 1. Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Users Table (Profiles)
CREATE TABLE users (
    id UUID REFERENCES auth.users ON DELETE CASCADE PRIMARY KEY,
    full_name TEXT NOT NULL,
    intended_major TEXT,
    club_deadlines JSONB, -- Flexible JSON to store multiple deadlines
    interest_embedding VECTOR(384),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS on users
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Users can only read and update their own profiles
CREATE POLICY "Users can view own profile" 
    ON users FOR SELECT 
    USING (auth.uid() = id);

CREATE POLICY "Users can update own profile" 
    ON users FOR UPDATE 
    USING (auth.uid() = id);

-- 3. Events Table
CREATE TABLE events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title TEXT NOT NULL,
    event_date TIMESTAMP WITH TIME ZONE NOT NULL,
    location TEXT NOT NULL,
    organization_name TEXT NOT NULL,
    qr_seed TEXT UNIQUE NOT NULL,
    description_embedding VECTOR(384),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS on events
ALTER TABLE events ENABLE ROW LEVEL SECURITY;

-- Events are public to read for any authenticated user
CREATE POLICY "Events are viewable by everyone" 
    ON events FOR SELECT 
    USING (true);

-- Only admins/service roles can insert/update events (enforced by lack of public INSERT policy)

-- 4. AttendanceLogs Table
CREATE TABLE attendance_logs (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE NOT NULL,
    event_id UUID REFERENCES events(id) ON DELETE CASCADE NOT NULL,
    check_in_timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    check_out_timestamp TIMESTAMP WITH TIME ZONE,
    awarded_hours NUMERIC,
    supervisor_name TEXT,
    supervisor_signature_url TEXT,
    status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'verified', 'rejected')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable RLS on attendance_logs
ALTER TABLE attendance_logs ENABLE ROW LEVEL SECURITY;

-- Users can only view, insert, and update their own attendance logs
CREATE POLICY "Users can view own logs" 
    ON attendance_logs FOR SELECT 
    USING (auth.uid() = user_id);

CREATE POLICY "Users can insert own logs" 
    ON attendance_logs FOR INSERT 
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "Users can update own logs" 
    ON attendance_logs FOR UPDATE 
    USING (auth.uid() = user_id);

-- 5. AI Vector Matching Function
CREATE OR REPLACE FUNCTION match_events(
    query_embedding VECTOR(384),
    match_threshold FLOAT DEFAULT 0.0,
    match_count INT DEFAULT 20
)
RETURNS TABLE (
    id UUID,
    title TEXT,
    organization_name TEXT,
    location TEXT,
    event_date TIMESTAMP WITH TIME ZONE,
    similarity FLOAT
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT
        e.id,
        e.title,
        e.organization_name,
        e.location,
        e.event_date,
        -- Calculate cosine similarity (1 - cosine distance)
        1 - (e.description_embedding <=> query_embedding) AS similarity
    FROM events e
    WHERE 1 - (e.description_embedding <=> query_embedding) > match_threshold
    ORDER BY e.description_embedding <=> query_embedding -- Order by distance ascending (similarity descending)
    LIMIT match_count;
END;
$$;
