ALTER TABLE weekly_approval
  ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

