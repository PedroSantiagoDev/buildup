CREATE TABLE projects
(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    client_name VARCHAR(255),
    description TEXT,
    start_date DATE,
    due_date DATE,
    contract_value DECIMAL(15, 2),
    down_payment DECIMAL(15, 2),
    cover_image_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',
    company_id UUID NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_projects_company FOREIGN KEY (company_id)
        REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_project_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ON_HOLD', 'CANCELLED')),
    CONSTRAINT chk_project_dates CHECK (due_date IS NULL OR start_date IS NULL OR due_date >= start_date),
    CONSTRAINT chk_contract_value CHECK (contract_value IS NULL OR contract_value >= 0),
    CONSTRAINT chk_down_payment CHECK (down_payment IS NULL OR down_payment >= 0)
);
