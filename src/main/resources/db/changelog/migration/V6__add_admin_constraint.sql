CREATE UNIQUE INDEX admin_idx
ON users (role)
WHERE (role = 'ADMIN')