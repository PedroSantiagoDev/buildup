-- V15: Add company_id to tasks and expenses for multi-tenant support

-- Add company_id to tasks
ALTER TABLE tasks ADD COLUMN company_id UUID;

-- Update existing tasks with company_id from their projects
UPDATE tasks t
SET company_id = p.company_id
FROM projects p
WHERE t.project_id = p.id;

-- Make company_id NOT NULL after populating
ALTER TABLE tasks ALTER COLUMN company_id SET NOT NULL;

-- Add index for tenant filtering
CREATE INDEX idx_tasks_company ON tasks(company_id);


-- Add company_id to expenses
ALTER TABLE expenses ADD COLUMN company_id UUID;

-- Update existing expenses with company_id from their projects
UPDATE expenses e
SET company_id = p.company_id
FROM projects p
WHERE e.project_id = p.id;

-- Make company_id NOT NULL after populating
ALTER TABLE expenses ALTER COLUMN company_id SET NOT NULL;

-- Add index for tenant filtering (already created in V14, so use IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_expenses_company ON expenses(company_id);
