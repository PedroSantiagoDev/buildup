-- V13: Create schedules and milestones tables

-- Create schedules table
CREATE TABLE schedules (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL UNIQUE,
    company_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    actual_start_date DATE,
    actual_end_date DATE,
    total_duration_days INTEGER,
    completed_percentage INTEGER DEFAULT 0,
    total_tasks INTEGER DEFAULT 0,
    completed_tasks INTEGER DEFAULT 0,
    overdue_tasks INTEGER DEFAULT 0,
    critical_path_duration INTEGER,
    last_calculated_at TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_on_track BOOLEAN DEFAULT true,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_schedule_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- Create milestones table
CREATE TABLE milestones (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    company_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    planned_date DATE NOT NULL,
    actual_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    completion_percentage INTEGER DEFAULT 0,
    order_index INTEGER,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_milestone_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_milestone_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_schedules_company ON schedules(company_id);
CREATE INDEX idx_schedules_project ON schedules(project_id);
CREATE INDEX idx_schedules_status ON schedules(status);
CREATE INDEX idx_schedules_end_date ON schedules(end_date);

CREATE INDEX idx_milestones_company ON milestones(company_id);
CREATE INDEX idx_milestones_project ON milestones(project_id);
CREATE INDEX idx_milestones_planned_date ON milestones(planned_date);
CREATE INDEX idx_milestones_status ON milestones(status);
CREATE INDEX idx_milestones_type ON milestones(type);
