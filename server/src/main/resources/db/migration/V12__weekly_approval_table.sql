-- ================================================
-- Migration: Create weekly_approval table
-- ================================================

-- 1. Create enum type for ApprovalStatus
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'approval_status') THEN
        CREATE TYPE approval_status AS ENUM ('PENDING', 'APPROVED', 'REVOKED');
    END IF;
END$$;

-- 2. Create table
CREATE TABLE IF NOT EXISTS weekly_approval (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,

    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,

    approved_by_id UUID NULL REFERENCES app_user(id) ON DELETE SET NULL,
    approved_at TIMESTAMPTZ NULL,

    status approval_status NOT NULL DEFAULT 'PENDING',
    notes TEXT NULL,

    CONSTRAINT unique_org_week UNIQUE (organization_id, week_start_date)
);

-- 3. Optional: Add index on status for filtering
CREATE INDEX IF NOT EXISTS idx_weekly_approval_status
    ON weekly_approval (status);

-- 4. Optional: Add index for fetching latest approvals quickly
CREATE INDEX IF NOT EXISTS idx_weekly_approval_org_startdate
    ON weekly_approval (organization_id, week_start_date DESC);
