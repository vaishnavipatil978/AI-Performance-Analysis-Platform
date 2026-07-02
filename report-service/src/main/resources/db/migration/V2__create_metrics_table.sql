CREATE TABLE report_metrics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    endpoint VARCHAR(500) NOT NULL,
    http_method VARCHAR(10),
    samples BIGINT NOT NULL DEFAULT 0,
    average_response_time_ms DECIMAL(12,2),
    min_response_time_ms DECIMAL(12,2),
    max_response_time_ms DECIMAL(12,2),
    p50_response_time_ms DECIMAL(12,2),
    p90_response_time_ms DECIMAL(12,2),
    p95_response_time_ms DECIMAL(12,2),
    p99_response_time_ms DECIMAL(12,2),
    throughput DECIMAL(12,4),
    error_rate DECIMAL(6,4),
    error_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_metrics_report_id ON report_metrics(report_id);
