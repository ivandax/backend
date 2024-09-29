INSERT INTO roles (role_id, role_name) VALUES (1, 'ADMIN')
    ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name) VALUES (2, 'DEV')
    ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name) VALUES (3, 'VIEWER')
    ON CONFLICT (role_id) DO NOTHING;