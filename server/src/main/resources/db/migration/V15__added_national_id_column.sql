ALTER TABLE app_user
DROP COLUMN secondary_phone_number,
ADD COLUMN national_id VARCHAR(13) UNIQUE NULL;