-- Insert a new user_type
insertUserType:
INSERT INTO user_type(id, created_at, updated_at, created_by, updated_by, displayname)
VALUES (?, ?, ?, ?, ?, ?);

-- Get all user_types
selectAllUserTypes:
SELECT * FROM user_type;

-- Get user_type by id
selectUserTypeById:
SELECT * FROM user_type
WHERE id = ?;

-- Delete user_type by id
deleteUserTypeById:
DELETE FROM user_type
WHERE id = ?;