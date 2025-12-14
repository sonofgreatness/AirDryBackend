DROP INDEX IF EXISTS unique_user_daily_access;

-- 2. Create the new composite unique index
--    This ensures uniqueness based on: User ID, Date, AND Route Path.
CREATE UNIQUE INDEX unique_user_daily_access
    ON user_daily_access (app_user_id, access_date, route_path);