-- Create phases table for schedule organization
CREATE TABLE phases (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    schedule_id UUID NOT NULL,
    company_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    actual_start_date DATE,
    actual_end_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    order_index INTEGER NOT NULL DEFAULT 0,
    completion_percentage INTEGER DEFAULT 0,
    duration_days INTEGER,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_phase_schedule FOREIGN KEY (schedule_id)
        REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_phase_company FOREIGN KEY (company_id)
        REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT chk_phase_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'DELAYED', 'CANCELLED')),
    CONSTRAINT chk_phase_completion_percentage CHECK (completion_percentage >= 0 AND completion_percentage <= 100)
);

-- Add phase_id column to tasks table
ALTER TABLE tasks ADD COLUMN phase_id UUID;

-- Add foreign key constraint
ALTER TABLE tasks ADD CONSTRAINT fk_task_phase
    FOREIGN KEY (phase_id) REFERENCES phases(id) ON DELETE SET NULL;

-- Create indexes for better performance
CREATE INDEX idx_phases_schedule_id ON phases(schedule_id);
CREATE INDEX idx_phases_company_id ON phases(company_id);
CREATE INDEX idx_phases_status ON phases(status);
CREATE INDEX idx_phases_order_index ON phases(order_index);
CREATE INDEX idx_tasks_phase_id ON tasks(phase_id);
