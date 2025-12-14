-- 🔽 Drop the unique constraint instead of the index
ALTER TABLE work_done_stack
DROP CONSTRAINT IF EXISTS uq_workdone_stack;

-- 🔽 Drop the quantity column if it exists
ALTER TABLE work_done_stack
DROP COLUMN IF EXISTS quantity;

-- 🔽 (Optional) Add a non-unique index for performance
CREATE INDEX IF NOT EXISTS idx_workdone_stack_workdoneid_stackid
    ON work_done_stack (work_done_id, stack_id);