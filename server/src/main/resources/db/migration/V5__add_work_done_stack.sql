-- ==========================================
-- Migration: V5__add_workdone_stack_schema.sql
-- Purpose: Introduce StackCode, Stack, WorkDone, and WorkDoneUser tables
-- ==========================================

-- 1. Create table: stack_code
CREATE TABLE IF NOT EXISTS stack_code (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    display_code VARCHAR(64) UNIQUE NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    created_by UUID NULL,
    updated_by UUID NULL
);

-- 2. Create table: work_done
CREATE TABLE IF NOT EXISTS work_done (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    created_by UUID NULL,
    updated_by UUID NULL
);

-- 3. Create table: stack
CREATE TABLE IF NOT EXISTS stack (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    length FLOAT NOT NULL,
    height FLOAT NOT NULL,
    thickness FLOAT NOT NULL,
    display_name VARCHAR(128) NOT NULL,

    -- Relationships
    stack_code_id UUID UNIQUE NOT NULL REFERENCES stack_code(id) ON DELETE CASCADE,
    work_done_id UUID NULL REFERENCES work_done(id) ON DELETE SET NULL,

    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    created_by UUID NULL,
    updated_by UUID NULL
);

-- 4. Create join table for many-to-many relation: work_done ↔ app_user
CREATE TABLE IF NOT EXISTS work_done_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    work_done_id UUID NOT NULL REFERENCES work_done(id) ON DELETE CASCADE,
    app_user_id UUID NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,

    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    created_by UUID NULL,
    updated_by UUID NULL,

    CONSTRAINT unique_workdone_user UNIQUE (work_done_id, app_user_id)
);

-- ==========================================
-- Indexes for faster joins and lookups
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_stack_stackcode ON stack(stack_code_id);
CREATE INDEX IF NOT EXISTS idx_stack_workdone ON stack(work_done_id);
CREATE INDEX IF NOT EXISTS idx_workdoneuser_workdone ON work_done_user(work_done_id);
CREATE INDEX IF NOT EXISTS idx_workdoneuser_appuser ON work_done_user(app_user_id);
