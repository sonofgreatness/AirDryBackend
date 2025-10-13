-- ==========================================
-- Migration: V8__add_workdone_stacks_join_table.sql
-- Purpose: Support many-to-many relation between WorkDone and Stacks
-- ==========================================

-- 1️⃣ Create the WorkDoneStacks join table
CREATE TABLE IF NOT EXISTS work_done_stack (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_done_id UUID NOT NULL REFERENCES work_done(id) ON DELETE CASCADE,
    stack_id UUID NOT NULL REFERENCES stack(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    CONSTRAINT uq_workdone_stack UNIQUE (work_done_id, stack_id)
);

-- 2️⃣ Optional: Migrate existing stack -> work_done associations
-- Only do this if you currently have workDoneId in stack table
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name='stack' AND column_name='work_done_id'
    ) THEN
        INSERT INTO work_done_stack (work_done_id, stack_id, created_at, updated_at)
        SELECT work_done_id, id, created_at, updated_at
        FROM stack
        WHERE work_done_id IS NOT NULL;
    END IF;
END $$;

-- 3️⃣ Drop the old work_done_id column from stack table
ALTER TABLE stack
    DROP COLUMN IF EXISTS work_done_id;

-- 4️⃣ Optional: Add comment for clarity
COMMENT ON TABLE work_done_stack IS 'Join table linking Stacks to WorkDone sessions for many-to-many relationship';
