-- V14: Add performance indexes to all tables

-- ============================================
-- USERS TABLE INDEXES
-- ============================================
-- Busca por email (login)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Busca por empresa
CREATE INDEX IF NOT EXISTS idx_users_company ON users(company_id);

-- Busca ativa (coluna não existe ainda)
-- CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);


-- ============================================
-- COMPANIES TABLE INDEXES
-- ============================================
-- Busca por documento (CNPJ)
CREATE INDEX IF NOT EXISTS idx_companies_document ON companies(document);

-- Busca por empresa ativa (coluna não existe ainda)
-- CREATE INDEX IF NOT EXISTS idx_companies_active ON companies(active);


-- ============================================
-- PROJECTS TABLE INDEXES
-- ============================================
-- Busca por empresa (muito usado!)
CREATE INDEX IF NOT EXISTS idx_projects_company ON projects(company_id);

-- Busca por status
CREATE INDEX IF NOT EXISTS idx_projects_status ON projects(status);

-- Busca por criador
CREATE INDEX IF NOT EXISTS idx_projects_created_by ON projects(created_by);

-- Busca por data de início
CREATE INDEX IF NOT EXISTS idx_projects_start_date ON projects(start_date);

-- Busca por data de término
CREATE INDEX IF NOT EXISTS idx_projects_due_date ON projects(due_date);

-- Índice composto para filtros comuns
CREATE INDEX IF NOT EXISTS idx_projects_company_status ON projects(company_id, status);


-- ============================================
-- PROJECT MEMBERS TABLE INDEXES
-- ============================================
-- Busca membros por projeto
CREATE INDEX IF NOT EXISTS idx_project_members_project ON project_members(project_id);

-- Busca projetos por usuário
CREATE INDEX IF NOT EXISTS idx_project_members_user ON project_members(user_id);

-- Índice composto (único por projeto-usuário já existe na PK, mas ajuda em buscas)
CREATE INDEX IF NOT EXISTS idx_project_members_project_user ON project_members(project_id, user_id);


-- ============================================
-- TASKS TABLE INDEXES
-- ============================================
-- Busca tarefas por projeto (muito usado!)
CREATE INDEX IF NOT EXISTS idx_tasks_project ON tasks(project_id);

-- Busca por status
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);

-- Busca por prioridade
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);

-- Busca tarefas atribuídas a usuário
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON tasks(assigned_to);

-- Busca por criador
CREATE INDEX IF NOT EXISTS idx_tasks_created_by ON tasks(created_by);

-- Busca por data de início
CREATE INDEX IF NOT EXISTS idx_tasks_start_date ON tasks(start_date);

-- Busca por data de término (para encontrar atrasadas)
CREATE INDEX IF NOT EXISTS idx_tasks_end_date ON tasks(end_date);

-- Ordenação por índice
CREATE INDEX IF NOT EXISTS idx_tasks_order ON tasks(project_id, order_index);

-- Índices compostos para queries comuns
CREATE INDEX IF NOT EXISTS idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_status ON tasks(assigned_to, status);
CREATE INDEX IF NOT EXISTS idx_tasks_project_priority ON tasks(project_id, priority);


-- ============================================
-- TASK DEPENDENCIES TABLE INDEXES
-- ============================================
-- Busca dependências de uma tarefa
CREATE INDEX IF NOT EXISTS idx_task_deps_task ON task_dependencies(task_id);

-- Busca tarefas que dependem de outra
CREATE INDEX IF NOT EXISTS idx_task_deps_depends_on ON task_dependencies(depends_on_task_id);

-- Busca por tipo
CREATE INDEX IF NOT EXISTS idx_task_deps_type ON task_dependencies(dependency_type);


-- ============================================
-- EXPENSES TABLE INDEXES
-- ============================================
-- Busca despesas por projeto
CREATE INDEX IF NOT EXISTS idx_expenses_project ON expenses(project_id);

-- Busca por categoria
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category_id);

-- Busca por status
CREATE INDEX IF NOT EXISTS idx_expenses_status ON expenses(status);

-- Busca por data de vencimento (para encontrar vencidas)
CREATE INDEX IF NOT EXISTS idx_expenses_due_date ON expenses(due_date);

-- Busca por data de pagamento
CREATE INDEX IF NOT EXISTS idx_expenses_paid_date ON expenses(paid_date);

-- Busca por criador
CREATE INDEX IF NOT EXISTS idx_expenses_created_by ON expenses(created_by);

-- Busca despesas parceladas
CREATE INDEX IF NOT EXISTS idx_expenses_installments ON expenses(has_installments);

-- Índices compostos para queries comuns
CREATE INDEX IF NOT EXISTS idx_expenses_project_status ON expenses(project_id, status);
CREATE INDEX IF NOT EXISTS idx_expenses_project_category ON expenses(project_id, category_id);
CREATE INDEX IF NOT EXISTS idx_expenses_status_due_date ON expenses(status, due_date);


-- ============================================
-- EXPENSE INSTALLMENTS TABLE INDEXES
-- ============================================
-- Busca parcelas por despesa
CREATE INDEX IF NOT EXISTS idx_installments_expense ON expense_installments(expense_id);

-- Busca por status
CREATE INDEX IF NOT EXISTS idx_installments_status ON expense_installments(status);

-- Busca por data de vencimento
CREATE INDEX IF NOT EXISTS idx_installments_due_date ON expense_installments(due_date);

-- Índice composto
CREATE INDEX IF NOT EXISTS idx_installments_expense_status ON expense_installments(expense_id, status);


-- ============================================
-- PAYMENT MILESTONES TABLE INDEXES (Table doesn't exist yet)
-- ============================================
-- Busca marcos por despesa
-- CREATE INDEX IF NOT EXISTS idx_payment_milestones_expense ON payment_milestones(expense_id);

-- Busca por status
-- CREATE INDEX IF NOT EXISTS idx_payment_milestones_status ON payment_milestones(status);

-- Busca por data esperada
-- CREATE INDEX IF NOT EXISTS idx_payment_milestones_expected_date ON payment_milestones(expected_date);


-- ============================================
-- ROLES TABLE INDEXES
-- ============================================
-- Busca por nome da role
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);


-- ============================================
-- USER ROLES TABLE INDEXES  
-- ============================================
-- Busca roles por usuário
CREATE INDEX IF NOT EXISTS idx_user_roles_user ON user_roles(user_id);

-- Busca usuários por role
CREATE INDEX IF NOT EXISTS idx_user_roles_role ON user_roles(role_id);
