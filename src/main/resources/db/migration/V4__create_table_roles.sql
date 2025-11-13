CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO roles (name, description) VALUES
    ('SUPER_ADMIN', 'Administrador Master do SaaS'),
    ('ADMIN', 'Administrador da Empresa'),
    ('MANAGER', 'Gerente de Obras'),
    ('USER', 'Usuário Básico');
