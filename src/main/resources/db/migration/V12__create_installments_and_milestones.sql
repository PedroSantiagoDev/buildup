CREATE TABLE expense_installments
(
    id UUID PRIMARY KEY,
    expense_id UUID NOT NULL,
    installment_number INT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    paid_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_expense_installments_expense FOREIGN KEY (expense_id)
        REFERENCES expenses(id) ON DELETE CASCADE,
    CONSTRAINT chk_installment_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')),
    CONSTRAINT chk_installment_amount CHECK (amount > 0),
    CONSTRAINT uk_expense_installment UNIQUE (expense_id, installment_number)
);

CREATE TABLE payment_milestones
(
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    milestone_number INT NOT NULL,
    description VARCHAR(500),
    value DECIMAL(15, 2) NOT NULL,
    invoice_number VARCHAR(100),
    due_date DATE NOT NULL,
    payment_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_payment_milestones_project FOREIGN KEY (project_id)
        REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT chk_milestone_status CHECK (status IN ('PENDING', 'PAID', 'LATE', 'CANCELLED')),
    CONSTRAINT chk_milestone_value CHECK (value > 0),
    CONSTRAINT uk_project_milestone UNIQUE (project_id, milestone_number)
);
