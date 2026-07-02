CREATE TABLE analysis_findings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    analysis_id UUID NOT NULL REFERENCES analyses(id) ON DELETE CASCADE,
    severity VARCHAR(20) NOT NULL,
    category VARCHAR(100) NOT NULL,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL,
    recommendation TEXT NOT NULL,
    affected_endpoint VARCHAR(500),
    metric_value VARCHAR(200),
    threshold_value VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_findings_analysis_id ON analysis_findings(analysis_id);
CREATE INDEX idx_findings_severity ON analysis_findings(severity);
