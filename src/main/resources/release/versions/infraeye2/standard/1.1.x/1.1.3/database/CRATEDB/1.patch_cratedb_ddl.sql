CREATE TABLE IF NOT EXISTS infraeye.server_metric_log (
    log_id TEXT,
    partition_day TEXT NOT NULL,
    collected_at TIMESTAMPTZ NOT NULL,
    policy_id INTEGER,
    detail_id INTEGER,
    mch_id INTEGER,
    agent_id INTEGER,
    host_name TEXT NOT NULL,
    host_ip TEXT NOT NULL,
    os_type TEXT NOT NULL,
    log_type TEXT NOT NULL,
    log_metadata TEXT INDEX USING FULLTEXT WITH (analyzer = 'standard'),
    log_contents TEXT INDEX USING FULLTEXT WITH (analyzer = 'standard'),
    log_written_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (log_id, partition_day)
)
PARTITIONED BY (partition_day)
CLUSTERED INTO 3 SHARDS
WITH (
    number_of_replicas = '1-2',
    column_policy = 'strict',
    refresh_interval = '1s'
);
