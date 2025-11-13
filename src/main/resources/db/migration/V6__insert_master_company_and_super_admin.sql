-- Criar empresa master (líder)
INSERT INTO companies (id, name, document, email, phone, is_master, is_active, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'BuildUp Master',
    '00000000000191',
    'master@buildup.com',
    '11999999999',
    true,
    true,
    NOW(),
    NOW()
);

-- Criar super admin (senha: Admin@123)
INSERT INTO users (id, name, email, password, company_id, is_active, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Super Admin',
    'superadmin@buildup.com',
    '$2a$10$jF5W1nqMb39RmOmwuD6.R.vTkt/gC7ysLkk5Vyhf/vYlJvl.AUa9y',
    '00000000-0000-0000-0000-000000000001',
    true,
    NOW(),
    NOW()
);

-- Vincular super admin ao role SUPER_ADMIN
INSERT INTO user_roles (user_id, role_id, assigned_at)
SELECT 
    '00000000-0000-0000-0000-000000000001',
    id,
    NOW()
FROM roles
WHERE name = 'SUPER_ADMIN';

-- Atualizar usuários existentes sem empresa para vincular à empresa master
UPDATE users 
SET company_id = '00000000-0000-0000-0000-000000000001'
WHERE company_id IS NULL;

-- Tornar company_id obrigatório
ALTER TABLE users ALTER COLUMN company_id SET NOT NULL;
