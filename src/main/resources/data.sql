INSERT INTO roles (role_id, role_name) VALUES (1, 'ADMIN')
    ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name) VALUES (2, 'DEV')
    ON CONFLICT (role_id) DO NOTHING;

INSERT INTO roles (role_id, role_name) VALUES (3, 'VIEWER')
    ON CONFLICT (role_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (1, 'dev')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (2, 'read:users')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (3, 'create:users')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (4, 'update:users')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (5, 'delete:users')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (6, 'create:todolist')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (7, 'read:todolist')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (8, 'update:todolist')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (9, 'read:self-user')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO permissions (permission_id, permission_name) VALUES (10, 'update:self-user')
    ON CONFLICT (permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 1)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 2)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 3)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 4)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 5)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 6)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 7)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 8)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 9)
    ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES (1, 10)
    ON CONFLICT DO NOTHING;