-- ----- WorkDoneUsers -----
-- Drop existing foreign key constraints
ALTER TABLE work_done_user
    DROP CONSTRAINT IF EXISTS work_done_user_work_done_id_fkey;

ALTER TABLE work_done_user
    DROP CONSTRAINT IF EXISTS work_done_user_app_user_id_fkey;

-- Recreate with proper ON DELETE rules
ALTER TABLE work_done_user
    ADD CONSTRAINT work_done_user_work_done_id_fkey
    FOREIGN KEY (work_done_id)
    REFERENCES work_done(id)
    ON DELETE CASCADE;

ALTER TABLE work_done_user
    ADD CONSTRAINT work_done_user_app_user_id_fkey
    FOREIGN KEY (app_user_id)
    REFERENCES app_user(id)
    ON DELETE RESTRICT;


-- ----- WorkDoneStacks -----
-- Drop existing foreign key constraints
ALTER TABLE work_done_stack
    DROP CONSTRAINT IF EXISTS work_done_stack_work_done_id_fkey;

ALTER TABLE work_done_stack
    DROP CONSTRAINT IF EXISTS work_done_stack_stack_id_fkey;

-- Recreate with proper ON DELETE rules
ALTER TABLE work_done_stack
    ADD CONSTRAINT work_done_stack_work_done_id_fkey
    FOREIGN KEY (work_done_id)
    REFERENCES work_done(id)
    ON DELETE CASCADE;

ALTER TABLE work_done_stack
    ADD CONSTRAINT work_done_stack_stack_id_fkey
    FOREIGN KEY (stack_id)
    REFERENCES stack(id)
    ON DELETE RESTRICT;