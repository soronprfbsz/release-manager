/*
 *
 * 대상 설치본 버전: InfraEye-2.0.0.241127-STD.tar.gz,  infra2_img_2.0.0_250204.tar.gz
 * 주요 내용: SMS 기능 추가에 따른 테이블 추가
 *
 */

CREATE TABLE IF NOT EXISTS "infraeye"."server_metric" (
   "collected_at" TIMESTAMP WITH TIME ZONE NOT NULL,
   "mch_id" TEXT NOT NULL,
   "partition_day" TEXT NOT NULL,
   "created_at" TIMESTAMP WITH TIME ZONE NOT NULL,
   "metric_type" TEXT NOT NULL,
   "cpu_metric" OBJECT(DYNAMIC) AS (
      "cpu_name" TEXT,
      "cpu_core_count" OBJECT(DYNAMIC) AS (
         "logical" INTEGER,
         "physical" INTEGER
      ),
      "cpu_idle_percent" DOUBLE PRECISION,
      "cpu_used_percent" DOUBLE PRECISION,
      "cpu_wait_percent" DOUBLE PRECISION,
      "cpu_user_percent" DOUBLE PRECISION,
      "cpu_system_percent" DOUBLE PRECISION,
      "cpu_nice_percent" DOUBLE PRECISION,
      "load_avg_1min_percent" DOUBLE PRECISION,
      "load_avg_5min_percent" DOUBLE PRECISION,
      "load_avg_15min_percent" DOUBLE PRECISION
   ),
   "mem_metric" OBJECT(DYNAMIC) AS (
      "mem_count" INTEGER,
      "mem_total_byte" BIGINT,
      "mem_free_byte" BIGINT,
      "mem_used_byte" BIGINT,
      "mem_free_percent" DOUBLE PRECISION,
      "mem_used_percent" DOUBLE PRECISION,
      "mem_cached_bytes" BIGINT,
      "mem_cached_percent" DOUBLE PRECISION,
      "mem_buffers_bytes" BIGINT,
      "mem_buffers_percent" DOUBLE PRECISION,
      "mem_swap_total_bytes" BIGINT,
      "mem_swap_free_bytes" BIGINT,
      "mem_swap_used_bytes" BIGINT,
      "mem_swap_used_percent" DOUBLE PRECISION,
      "mem_swap_cached_bytes" BIGINT,
      "mem_swap_cached_percent" DOUBLE PRECISION,
      "mem_used_bytes" BIGINT,
      "mem_free_bytes" BIGINT,
      "mem_total_bytes" BIGINT
   ),
   "disk_metric" OBJECT(DYNAMIC) AS (
      "total_disk_count" INTEGER,
      "total_partition_count" INTEGER,
      "total_disk_used_percent" DOUBLE PRECISION,
      "disk_info" ARRAY(OBJECT(DYNAMIC) AS (
         "disk_name" TEXT,
         "disk_model" TEXT,
         "read_bytes" BIGINT,
         "write_bytes" BIGINT,
         "read_count" BIGINT,
         "write_count" BIGINT,
         "busy_rate_percent" DOUBLE PRECISION,
         "disk_total_bytes" BIGINT,
         "disk_used_bytes" BIGINT,
         "disk_free_bytes" BIGINT,
         "disk_used_percent" DOUBLE PRECISION,
         "partition_info" ARRAY(OBJECT(DYNAMIC) AS (
            "partition_name" TEXT,
            "mount_point" TEXT,
            "mount_type" TEXT,
            "fs_type" TEXT,
            "partition_total_bytes" BIGINT,
            "partition_used_bytes" BIGINT,
            "partition_free_bytes" BIGINT,
            "partition_used_percent" DOUBLE PRECISION,
            "uid" TEXT
         )),
         "uid" TEXT
      ))
   ),
   "network_metric" OBJECT(DYNAMIC) AS (
      "nic_info" ARRAY(OBJECT(DYNAMIC) AS (
         "nic_name" TEXT,
         "nic_ip" TEXT,
         "nic_ipv6" TEXT,
         "nic_mac" TEXT,
         "nic_comment" TEXT,
         "nic_status" TEXT,
         "nic_speed" BIGINT,
         "nic_duplex" TEXT,
         "nic_metric" OBJECT(DYNAMIC) AS (
            "in_bps" BIGINT,
            "out_bps" BIGINT,
            "in_pps" BIGINT,
            "out_pps" BIGINT,
            "in_err_count" BIGINT,
            "out_err_count" BIGINT
         ),
         "uid" TEXT,
         "in_bps" REAL,
         "in_err_count" BIGINT,
         "in_pps" REAL,
         "out_bps" REAL,
         "out_err_count" BIGINT,
         "out_pps" REAL
      ))
   ),
   "service_metric" OBJECT(DYNAMIC) AS (
      "session_count" INTEGER,
      "port_info" ARRAY(OBJECT(DYNAMIC) AS (
         "port_number" INTEGER,
         "port_status_count" OBJECT(DYNAMIC) AS (
            "listen_count" INTEGER,
            "syn_sent_count" INTEGER,
            "syn_recv_count" INTEGER,
            "established_count" INTEGER,
            "fin_wait1_count" INTEGER,
            "fin_wait2_count" INTEGER,
            "close_wait_count" INTEGER,
            "closing_count" INTEGER,
            "closed_count" INTEGER,
            "last_ack_count" INTEGER,
            "time_wait_count" INTEGER,
            "unknown_count" INTEGER
         ),
         "connections" ARRAY(OBJECT(DYNAMIC) AS (
            "status" TEXT,
            "local_addr" TEXT,
            "peer_addr" TEXT,
            "program" TEXT,
            "protocol" TEXT,
            "uid" TEXT
         )),
         "uid" TEXT,
         "service_name" TEXT
      ))
   ),
   "process_metric" OBJECT(DYNAMIC) AS (
      "process_count" BIGINT,
      "process_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "name" TEXT,
         "pid" INTEGER,
         "start_time" TIMESTAMP WITH TIME ZONE,
         "run_time" DOUBLE PRECISION,
         "nice" BIGINT,
         "priority" INTEGER,
         "process_count" INTEGER,
         "thread_count" INTEGER,
         "cpu_used_percent" DOUBLE PRECISION,
         "mem_used_percent" DOUBLE PRECISION,
         "mem_used_bytes" BIGINT,
         "io_bytes" BIGINT,
         "user_name" TEXT,
         "process_command" TEXT,
         "status" TEXT,
			   "status_detail" TEXT
      ))
   ),
   "mch_ip" TEXT,
   PRIMARY KEY ("collected_at", "mch_id", "partition_day")
)
CLUSTERED INTO 3 SHARDS
PARTITIONED BY ("partition_day")
WITH (
   "allocation.max_retries" = 5,
   "blocks.metadata" = false,
   "blocks.read" = false,
   "blocks.read_only" = false,
   "blocks.read_only_allow_delete" = false,
   "blocks.write" = false,
   codec = 'default',
   column_policy = 'dynamic',
   "mapping.total_fields.limit" = 2000,
   max_ngram_diff = 1,
   max_shingle_diff = 3,
   number_of_replicas = '0',
   refresh_interval = 10000,
   "routing.allocation.enable" = 'all',
   "routing.allocation.total_shards_per_node" = -1,
   "store.type" = 'fs',
   "translog.durability" = 'ASYNC',
   "translog.flush_threshold_size" = 536870912,
   "translog.sync_interval" = 1000,
   "unassigned.node_left.delayed_timeout" = 60000,
   "write.wait_for_active_shards" = '1'
);

--------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS "infraeye"."server_metric_1h" (
   "collected_at" TIMESTAMP WITH TIME ZONE NOT NULL,
   "mch_id" TEXT NOT NULL,
   "metric_type" TEXT NOT NULL,
   "partition_day" TIMESTAMP WITH TIME ZONE GENERATED ALWAYS AS date_trunc('month', "collected_at") NOT NULL,
   "created_at" TIMESTAMP WITH TIME ZONE NOT NULL,
   "cpu_metric" OBJECT(DYNAMIC) AS (
      "cpu_name" TEXT,
      "cpu_core_count" OBJECT(DYNAMIC) AS (
         "logical" INTEGER,
         "physical" INTEGER
      ),
      "cpu_idle_percent" DOUBLE PRECISION,
      "cpu_used_percent" DOUBLE PRECISION,
      "cpu_wait_percent" DOUBLE PRECISION,
      "cpu_user_percent" DOUBLE PRECISION,
      "cpu_system_percent" DOUBLE PRECISION,
      "cpu_nice_percent" DOUBLE PRECISION,
      "load_avg_1min_percent" DOUBLE PRECISION,
      "load_avg_5min_percent" DOUBLE PRECISION,
      "load_avg_15min_percent" DOUBLE PRECISION
   ),
   "mem_metric" OBJECT(DYNAMIC) AS (
      "mem_count" INTEGER,
      "mem_total_bytes" BIGINT,
      "mem_free_bytes" BIGINT,
      "mem_used_bytes" BIGINT,
      "mem_free_percent" DOUBLE PRECISION,
      "mem_used_percent" DOUBLE PRECISION,
      "mem_cached_bytes" BIGINT,
      "mem_cached_percent" DOUBLE PRECISION,
      "mem_buffers_bytes" BIGINT,
      "mem_buffers_percent" DOUBLE PRECISION,
      "mem_swap_total_bytes" BIGINT,
      "mem_swap_free_bytes" BIGINT,
      "mem_swap_used_bytes" BIGINT,
      "mem_swap_used_percent" DOUBLE PRECISION,
      "mem_swap_cached_bytes" BIGINT,
      "mem_swap_cached_percent" DOUBLE PRECISION
   ),
   "disk_metric" OBJECT(DYNAMIC) AS (
      "total_disk_count" INTEGER,
      "total_partition_count" INTEGER,
      "total_disk_used_percent" DOUBLE PRECISION,
      "disk_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "disk_name" TEXT,
         "disk_model" TEXT,
         "read_bytes" BIGINT,
         "write_bytes" BIGINT,
         "read_count" BIGINT,
         "write_count" BIGINT,
         "busy_rate_percent" DOUBLE PRECISION,
         "disk_total_bytes" BIGINT,
         "disk_used_bytes" BIGINT,
         "disk_free_bytes" BIGINT,
         "disk_used_percent" DOUBLE PRECISION,
         "partition_info" ARRAY(OBJECT(DYNAMIC) AS (
            "uid" TEXT,
            "partition_name" TEXT,
            "mount_point" TEXT,
            "mount_type" TEXT,
            "fs_type" TEXT,
            "partition_total_bytes" BIGINT,
            "partition_used_bytes" BIGINT,
            "partition_free_bytes" BIGINT,
            "partition_used_percent" DOUBLE PRECISION
         ))
      ))
   ),
   "network_metric" OBJECT(DYNAMIC) AS (
      "nic_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "nic_name" TEXT,
         "nic_ip" TEXT,
         "nic_ipv6" TEXT,
         "nic_mac" TEXT,
         "nic_comment" TEXT,
         "nic_status" TEXT,
         "nic_speed" BIGINT,
         "nic_duplex" TEXT,
         "nic_metric" OBJECT(DYNAMIC) AS (
            "in_bps" BIGINT,
            "out_bps" BIGINT,
            "in_pps" BIGINT,
            "out_pps" BIGINT,
            "in_err_count" BIGINT,
            "out_err_count" BIGINT
         )
      ))
   ),
   "service_metric" OBJECT(DYNAMIC) AS (
      "session_count" INTEGER,
      "port_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "service_name" TEXT,
         "port_number" INTEGER,
         "port_status_count" OBJECT(DYNAMIC) AS (
            "listen_count" INTEGER,
            "syn_sent_count" INTEGER,
            "syn_recv_count" INTEGER,
            "established_count" INTEGER,
            "fin_wait1_count" INTEGER,
            "fin_wait2_count" INTEGER,
            "close_wait_count" INTEGER,
            "closing_count" INTEGER,
            "closed_count" INTEGER,
            "last_ack_count" INTEGER,
            "time_wait_count" INTEGER,
            "unknown_count" INTEGER
         ),
         "connections" ARRAY(OBJECT(DYNAMIC) AS (
            "uid" TEXT,
            "status" TEXT,
            "local_addr" TEXT,
            "peer_addr" TEXT,
            "program" TEXT,
            "protocol" TEXT
         ))
      ))
   ),
   "process_metric" OBJECT(DYNAMIC) AS (
      "process_count" BIGINT,
      "process_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "name" TEXT,
         "pid" INTEGER,
         "start_time" TIMESTAMP WITH TIME ZONE,
         "run_time" DOUBLE PRECISION,
         "nice" BIGINT,
         "priority" INTEGER,
         "process_count" INTEGER,
         "thread_count" INTEGER,
         "cpu_used_percent" DOUBLE PRECISION,
         "mem_used_percent" DOUBLE PRECISION,
         "mem_used_bytes" BIGINT,
         "io_bytes" BIGINT,
         "user_name" TEXT,
         "process_command" TEXT,
         "status" TEXT,
			   "status_detail" TEXT
      ))
   ),
   PRIMARY KEY ("collected_at", "mch_id", "metric_type", "partition_day")
)
CLUSTERED INTO 3 SHARDS
PARTITIONED BY ("partition_day")
WITH (
   "allocation.max_retries" = 5,
   "blocks.metadata" = false,
   "blocks.read" = false,
   "blocks.read_only" = false,
   "blocks.read_only_allow_delete" = false,
   "blocks.write" = false,
   codec = 'default',
   column_policy = 'dynamic',
   "mapping.total_fields.limit" = 2000,
   max_ngram_diff = 1,
   max_shingle_diff = 3,
   number_of_replicas = '0',
   refresh_interval = 10000,
   "routing.allocation.enable" = 'all',
   "routing.allocation.total_shards_per_node" = -1,
   "store.type" = 'fs',
   "translog.durability" = 'ASYNC',
   "translog.flush_threshold_size" = 536870912,
   "translog.sync_interval" = 1000,
   "unassigned.node_left.delayed_timeout" = 60000,
   "write.wait_for_active_shards" = '1'
);

--------------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS "infraeye"."server_metric_1d" (
   "collected_at" TIMESTAMP WITH TIME ZONE NOT NULL,
   "mch_id" TEXT NOT NULL,
   "metric_type" TEXT NOT NULL,
   "partition_day" TIMESTAMP WITH TIME ZONE GENERATED ALWAYS AS date_trunc('month', "collected_at") NOT NULL,
   "created_at" TIMESTAMP WITH TIME ZONE NOT NULL,
   "cpu_metric" OBJECT(DYNAMIC) AS (
      "cpu_name" TEXT,
      "cpu_core_count" OBJECT(DYNAMIC) AS (
         "logical" INTEGER,
         "physical" INTEGER
      ),
      "cpu_idle_percent" DOUBLE PRECISION,
      "cpu_used_percent" DOUBLE PRECISION,
      "cpu_wait_percent" DOUBLE PRECISION,
      "cpu_user_percent" DOUBLE PRECISION,
      "cpu_system_percent" DOUBLE PRECISION,
      "cpu_nice_percent" DOUBLE PRECISION,
      "load_avg_1min_percent" DOUBLE PRECISION,
      "load_avg_5min_percent" DOUBLE PRECISION,
      "load_avg_15min_percent" DOUBLE PRECISION
   ),
   "mem_metric" OBJECT(DYNAMIC) AS (
      "mem_count" INTEGER,
      "mem_total_bytes" BIGINT,
      "mem_free_bytes" BIGINT,
      "mem_used_bytes" BIGINT,
      "mem_free_percent" DOUBLE PRECISION,
      "mem_used_percent" DOUBLE PRECISION,
      "mem_cached_bytes" BIGINT,
      "mem_cached_percent" DOUBLE PRECISION,
      "mem_buffers_bytes" BIGINT,
      "mem_buffers_percent" DOUBLE PRECISION,
      "mem_swap_total_bytes" BIGINT,
      "mem_swap_free_bytes" BIGINT,
      "mem_swap_used_bytes" BIGINT,
      "mem_swap_used_percent" DOUBLE PRECISION,
      "mem_swap_cached_bytes" BIGINT,
      "mem_swap_cached_percent" DOUBLE PRECISION
   ),
   "disk_metric" OBJECT(DYNAMIC) AS (
      "total_disk_count" INTEGER,
      "total_partition_count" INTEGER,
      "total_disk_used_percent" DOUBLE PRECISION,
      "disk_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "disk_name" TEXT,
         "disk_model" TEXT,
         "read_bytes" BIGINT,
         "write_bytes" BIGINT,
         "read_count" BIGINT,
         "write_count" BIGINT,
         "busy_rate_percent" DOUBLE PRECISION,
         "disk_total_bytes" BIGINT,
         "disk_used_bytes" BIGINT,
         "disk_free_bytes" BIGINT,
         "disk_used_percent" DOUBLE PRECISION,
         "partition_info" ARRAY(OBJECT(DYNAMIC) AS (
            "uid" TEXT,
            "partition_name" TEXT,
            "mount_point" TEXT,
            "mount_type" TEXT,
            "fs_type" TEXT,
            "partition_total_bytes" BIGINT,
            "partition_used_bytes" BIGINT,
            "partition_free_bytes" BIGINT,
            "partition_used_percent" DOUBLE PRECISION
         ))
      ))
   ),
   "network_metric" OBJECT(DYNAMIC) AS (
      "nic_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "nic_name" TEXT,
         "nic_ip" TEXT,
         "nic_ipv6" TEXT,
         "nic_mac" TEXT,
         "nic_comment" TEXT,
         "nic_status" TEXT,
         "nic_speed" BIGINT,
         "nic_duplex" TEXT,
         "nic_metric" OBJECT(DYNAMIC) AS (
            "in_bps" BIGINT,
            "out_bps" BIGINT,
            "in_pps" BIGINT,
            "out_pps" BIGINT,
            "in_err_count" BIGINT,
            "out_err_count" BIGINT
         )
      ))
   ),
   "service_metric" OBJECT(DYNAMIC) AS (
      "session_count" INTEGER,
      "port_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "service_name" TEXT,
         "port_number" INTEGER,
         "port_status_count" OBJECT(DYNAMIC) AS (
            "listen_count" INTEGER,
            "syn_sent_count" INTEGER,
            "syn_recv_count" INTEGER,
            "established_count" INTEGER,
            "fin_wait1_count" INTEGER,
            "fin_wait2_count" INTEGER,
            "close_wait_count" INTEGER,
            "closing_count" INTEGER,
            "closed_count" INTEGER,
            "last_ack_count" INTEGER,
            "time_wait_count" INTEGER,
            "unknown_count" INTEGER
         ),
         "connections" ARRAY(OBJECT(DYNAMIC) AS (
            "uid" TEXT,
            "status" TEXT,
            "local_addr" TEXT,
            "peer_addr" TEXT,
            "program" TEXT,
            "protocol" TEXT
         ))
      ))
   ),
   "process_metric" OBJECT(DYNAMIC) AS (
      "process_count" BIGINT,
      "process_info" ARRAY(OBJECT(DYNAMIC) AS (
         "uid" TEXT,
         "name" TEXT,
         "pid" INTEGER,
         "start_time" TIMESTAMP WITH TIME ZONE,
         "run_time" DOUBLE PRECISION,
         "nice" BIGINT,
         "priority" INTEGER,
         "process_count" INTEGER,
         "thread_count" INTEGER,
         "cpu_used_percent" DOUBLE PRECISION,
         "mem_used_percent" DOUBLE PRECISION,
         "mem_used_bytes" BIGINT,
         "io_bytes" BIGINT,
         "user_name" TEXT,
         "process_command" TEXT,
         "status" TEXT,
			   "status_detail" TEXT
      ))
   ),
   PRIMARY KEY ("collected_at", "mch_id", "metric_type", "partition_day")
)
CLUSTERED INTO 3 SHARDS
PARTITIONED BY ("partition_day")
WITH (
   "allocation.max_retries" = 5,
   "blocks.metadata" = false,
   "blocks.read" = false,
   "blocks.read_only" = false,
   "blocks.read_only_allow_delete" = false,
   "blocks.write" = false,
   codec = 'default',
   column_policy = 'dynamic',
   "mapping.total_fields.limit" = 2000,
   max_ngram_diff = 1,
   max_shingle_diff = 3,
   number_of_replicas = '0',
   refresh_interval = 10000,
   "routing.allocation.enable" = 'all',
   "routing.allocation.total_shards_per_node" = -1,
   "store.type" = 'fs',
   "translog.durability" = 'ASYNC',
   "translog.flush_threshold_size" = 536870912,
   "translog.sync_interval" = 1000,
   "unassigned.node_left.delayed_timeout" = 60000,
   "write.wait_for_active_shards" = '1'
);

--------------------------------------------------------------------------------

CREATE TABLE infraeye.server_metric_log (
    collected_at TIMESTAMP WITH TIME ZONE NOT NULL,                -- 실제 수집 시각
    partition_day TEXT NOT NULL,
    policy_id int4,                           -- 정책 ID
    detail_id int4,                           -- 상세 ID
    agent_id int,
    host_name TEXT NOT NULL,                          -- 호스트명
    host_ip TEXT NOT NULL,                              -- CrateDB는 IP 타입 지원 (검색 최적화)
    os_type TEXT NOT NULL,                            -- OS 타입
    log_type TEXT NOT NULL,                           -- 로그 유형 (INFO / ERROR / etc.)
    log_contents TEXT INDEX USING FULLTEXT WITH (analyzer = 'standard'),  -- 전문 검색용
    log_written_at timestamptz,               -- 로그가 기록된 시각
    created_at timestamptz DEFAULT CURRENT_TIMESTAMP,  -- 생성 시각
    created_by TEXT NOT NULL,                         -- 생성자 정보
    PRIMARY KEY (partition_day, agent_id, collected_at)
)
CLUSTERED INTO 3 SHARDS
PARTITIONED BY ("partition_day")
WITH (
    number_of_replicas = '1-2',                   -- 자동 복제 (가용성 향상)
    column_policy = 'strict',                     -- 예기치 않은 컬럼 추가 방지
    refresh_interval = '1s'                       -- 거의 실시간으로 검색 가능
);
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
SELECT 'DDL 패치 완료' AS RESULT;
