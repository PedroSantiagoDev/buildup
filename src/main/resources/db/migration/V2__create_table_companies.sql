CREATE TABLE companies
(
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    document VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    logo_url VARCHAR(500),
    is_master BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    master_company_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_companies_master FOREIGN KEY (master_company_id) REFERENCES companies(id)
);
