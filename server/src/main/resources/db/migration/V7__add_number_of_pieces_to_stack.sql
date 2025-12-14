-- ==========================================
-- Migration: V7__add_stack_pieces_and_update_volume.sql
-- Purpose: Add "number_of_pieces" to stack and update volume formula
-- ==========================================

DO $$
BEGIN
    -- Safety check: ensure the stack table exists before we modify it
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'stack') THEN
        RAISE EXCEPTION 'Table "stack" does not exist. Ensure V5__add_workdone_stack.sql and V6__add_stack_volume_generated_column.sql have run.';
    END IF;
END $$;

-- ==========================================
-- Step 1: Add new "number_of_pieces" column
-- Default to 1 for existing rows so volume formula remains valid
-- ==========================================
ALTER TABLE stack
    ADD COLUMN IF NOT EXISTS number_of_pieces INTEGER NOT NULL DEFAULT 1;

COMMENT ON COLUMN stack.number_of_pieces IS
    'Number of timber pieces in the stack. Used in volume computation (length × height × thickness × number_of_pieces).';

-- ==========================================
-- Step 2: Drop and recreate the generated column for updated formula
-- We must drop the existing generated column to redefine it
-- ==========================================

-- Drop the old volume column if it already exists (it’s generated)
ALTER TABLE stack
    DROP COLUMN IF EXISTS volume;

-- Recreate volume as a generated column using the new formula
ALTER TABLE stack
    ADD COLUMN volume DOUBLE PRECISION
        GENERATED ALWAYS AS (length * height * thickness * number_of_pieces) STORED;

COMMENT ON COLUMN stack.volume IS
    'Auto-calculated as length × height × thickness × number_of_pieces (in cubic meters).';

-- ==========================================
-- Step 3: Validation Constraints
-- Ensure positive dimensions and piece count
-- ==========================================
ALTER TABLE stack
    DROP CONSTRAINT IF EXISTS chk_stack_dimensions_positive;

ALTER TABLE stack
    ADD CONSTRAINT chk_stack_dimensions_positive
    CHECK (
        length > 0 AND height > 0 AND thickness > 0 AND number_of_pieces > 0
    );
