CREATE TABLE IF NOT EXISTS monthly_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,

    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    upload_time TIMESTAMPTZ NOT NULL,
    file_name TEXT  NOT NULL,
    file_data BYTEA  NOT NULL
);