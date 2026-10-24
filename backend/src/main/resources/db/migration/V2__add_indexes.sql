-- V2: Add indexes for common query patterns
-- completed is filtered often; created_at is used for default sorting
CREATE INDEX idx_tasks_completed ON tasks (completed);
CREATE INDEX idx_tasks_created_at ON tasks (created_at DESC);
CREATE INDEX idx_tasks_priority ON tasks (priority);
CREATE INDEX idx_tasks_due_date ON tasks (due_date);
