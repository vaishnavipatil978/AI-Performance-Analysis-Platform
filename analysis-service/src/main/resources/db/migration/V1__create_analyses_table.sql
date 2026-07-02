CREATE TABLE analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL,
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    ai_model VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_analyses_report_id ON analyses(report_id);
CREATE INDEX idx_analyses_user_id ON analyses(user_id);
CREATE INDEX idx_analyses_status ON analyses(status);
