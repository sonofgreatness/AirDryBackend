-- 1) Add quantity column (default 1)
ALTER TABLE work_done_stack ADD COLUMN IF NOT EXISTS quantity integer DEFAULT 1;
