-- =====================================================================
-- Migration 003: Add missing supervisor contact fields
-- =====================================================================

ALTER TABLE attendance_logs ADD COLUMN IF NOT EXISTS supervisor_email TEXT;
ALTER TABLE attendance_logs ADD COLUMN IF NOT EXISTS supervisor_phone TEXT;

-- Note: You also need to create a storage bucket named 'signatures' 
-- in your Supabase dashboard and configure its access policies to allow 
-- authenticated users to upload and read images.
