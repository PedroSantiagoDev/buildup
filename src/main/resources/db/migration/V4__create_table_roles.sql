CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO roles (id, name, description) VALUES
    ('00000000-0000-0000-0000-000000000001', 'SUPER_ADMIN', 'Administrador Master do SaaS'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN', 'Administrador da Empresa'),
    ('00000000-0000-0000-0000-000000000003', 'MANAGER', 'Gerente de Obras'),
    ('00000000-0000-0000-0000-000000000004', 'USER', 'Usuário Básico');
