CREATE TABLE expense_categories
(
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon VARCHAR(50),
    color VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO expense_categories (id, name, description, icon, color) VALUES
    (gen_random_uuid(), 'MAO_DE_OBRA', 'Mão de Obra', 'users', '#3B82F6'),
    (gen_random_uuid(), 'MATERIAIS', 'Materiais de Construção', 'package', '#10B981'),
    (gen_random_uuid(), 'EQUIPAMENTOS', 'Equipamentos e Ferramentas', 'tool', '#F59E0B'),
    (gen_random_uuid(), 'TRANSPORTE', 'Transporte e Frete', 'truck', '#6366F1'),
    (gen_random_uuid(), 'ALIMENTACAO', 'Alimentação', 'coffee', '#EC4899'),
    (gen_random_uuid(), 'ADMINISTRATIVO', 'Despesas Administrativas', 'briefcase', '#8B5CF6'),
    (gen_random_uuid(), 'IMPOSTOS', 'Impostos e Taxas', 'file-text', '#EF4444'),
    (gen_random_uuid(), 'ALUGUEL', 'Aluguel de Equipamentos', 'home', '#14B8A6'),
    (gen_random_uuid(), 'SERVICOS', 'Serviços Terceirizados', 'users', '#F97316'),
    (gen_random_uuid(), 'OUTROS', 'Outros', 'more-horizontal', '#6B7280');

CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    category_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    supplier VARCHAR(255),
    invoice_number VARCHAR(100),
    invoice_url VARCHAR(500),
    notes TEXT,
    has_installments BOOLEAN DEFAULT FALSE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_expenses_project FOREIGN KEY (project_id)
        REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_expenses_category FOREIGN KEY (category_id)
        REFERENCES expense_categories(id),
    CONSTRAINT fk_expenses_created_by FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_expense_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT chk_payment_method CHECK (payment_method IN ('DINHEIRO', 'PIX', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'BOLETO', 'TRANSFERENCIA', 'CHEQUE')),
    CONSTRAINT chk_expense_amount CHECK (amount > 0)
);
