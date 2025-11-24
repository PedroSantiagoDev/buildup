CREATE TABLE project_chat_messages (
    id UUID NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE,
    updated_at TIMESTAMP WITHOUT TIME ZONE,

    content TEXT NOT NULL,
    type VARCHAR(50) DEFAULT 'TEXT',

    company_id UUID NOT NULL,
    project_id UUID NOT NULL,
    sender_id UUID NOT NULL,

    CONSTRAINT pk_project_chat_messages PRIMARY KEY (id),
    CONSTRAINT fk_company_chat_messages FOREIGN KEY (company_id)
        REFERENCES companies(id) ON DELETE CASCADE
);

ALTER TABLE project_chat_messages
    ADD CONSTRAINT fk_chat_messages_project
    FOREIGN KEY (project_id)
    REFERENCES projects (id);

ALTER TABLE project_chat_messages
    ADD CONSTRAINT fk_chat_messages_sender
    FOREIGN KEY (sender_id)
    REFERENCES users (id);

CREATE INDEX idx_chat_messages_project_id ON project_chat_messages (project_id);

CREATE INDEX idx_chat_messages_project_date ON project_chat_messages (project_id, created_at DESC);