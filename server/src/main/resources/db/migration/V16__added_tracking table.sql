
CREATE TABLE user_daily_access (
    -- Columns inherited from BaseTable
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by UUID NULL,
    updated_by UUID NULL,

    -- Foreign Key to the AppUser
    app_user_id UUID NOT NULL,
    -- Tracking data
    access_date DATE NOT NULL,
    access_count INTEGER NOT NULL DEFAULT 1,
    route_path VARCHAR(255) NOT NULL, -- Exposed Varchar(255) maps to SQL VARCHAR

    -- Foreign Key Constraint (onDelete = ReferenceOption.CASCADE)
    CONSTRAINT fk_user_daily_access_app_user_id
        FOREIGN KEY (app_user_id)
        REFERENCES app_user (id)
        ON DELETE CASCADE
);
CREATE UNIQUE INDEX unique_user_daily_access
    ON user_daily_access (app_user_id, access_date);