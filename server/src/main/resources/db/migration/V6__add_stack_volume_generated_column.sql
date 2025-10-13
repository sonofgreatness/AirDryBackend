-- ==========================================
-- Migration: V6__add_stack_volume_generated_column.sql
-- Purpose: Add a computed "volume" column to the stack table
-- ==========================================

-- Safety check: ensure the stack table exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'stack') THEN
        RAISE EXCEPTION 'Table "stack" does not exist. Run V5__add_workdone_stack.sql first.';
    END IF;
END $$;

-- Add generated (computed) column for volume: length × height × thickness
ALTER TABLE stack
ADD COLUMN IF NOT EXISTS volume DOUBLE PRECISION GENERATED ALWAYS AS (length * height * thickness) STORED;

-- Optional: create index for fast aggregation/reporting on volume
CREATE INDEX IF NOT EXISTS idx_stack_volume ON stack(volume);

-- ==========================================
-- Optional Example Check Constraint (Data Validation)
-- Ensures no negative or zero dimensions
-- ==========================================
ALTER TABLE stack
    ADD CONSTRAINT chk_stack_dimensions_positive
    CHECK (length > 0 AND height > 0 AND thickness > 0);

-- ==========================================
-- Optional Comment for Documentation
-- ==========================================
COMMENT ON COLUMN stack.volume IS 'Auto-calculated as length × height × thickness (in cubic meters).';
