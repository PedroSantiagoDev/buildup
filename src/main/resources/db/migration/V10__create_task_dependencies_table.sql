CREATE TABLE task_dependencies
(
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL,
    depends_on_task_id UUID NOT NULL,
    dependency_type VARCHAR(50) DEFAULT 'FINISH_TO_START',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_task_dependencies_task FOREIGN KEY (task_id)
        REFERENCES tasks(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_dependencies_depends_on FOREIGN KEY (depends_on_task_id)
        REFERENCES tasks(id) ON DELETE CASCADE,

    CONSTRAINT chk_no_self_dependency CHECK (task_id != depends_on_task_id),
    CONSTRAINT chk_dependency_type CHECK (dependency_type IN ('FINISH_TO_START', 'START_TO_START', 'FINISH_TO_FINISH', 'START_TO_FINISH')),
    CONSTRAINT uk_task_dependency UNIQUE (task_id, depends_on_task_id)
);
