-- =========================================================
-- ClickHouse 초기화 스크립트
-- =========================================================
-- 터미널 명령어 실행 이력 저장용 데이터베이스 및 테이블 생성
-- =========================================================

-- 데이터베이스 생성 (환경변수로 생성되므로 이미 존재)
-- CREATE DATABASE IF NOT EXISTS terminal_history;

-- terminal_history 데이터베이스 사용
-- USE terminal_history;

-- 터미널 명령어 이력 테이블 생성
CREATE TABLE IF NOT EXISTS terminal_history.terminal_command_history
(
    event_time DateTime DEFAULT now() COMMENT '이벤트 발생 시간',
    shell_session_id String COMMENT '셸 세션 ID (UUID)',
    user_email LowCardinality(String) COMMENT '사용자 이메일',
    host LowCardinality(String) COMMENT '접속 호스트',
    port UInt16 COMMENT 'SSH 포트',
    command String COMMENT '실행 명령어',
    output String COMMENT '명령어 출력 결과',
    exit_code Nullable(Int32) COMMENT '종료 코드',
    execution_duration_ms UInt32 COMMENT '실행 시간 (밀리초)',
    is_success UInt8 DEFAULT 1 COMMENT '성공 여부 (1: 성공, 0: 실패)',

    -- 인덱스 정의
    INDEX idx_user_email user_email TYPE bloom_filter GRANULARITY 4,
    INDEX idx_host host TYPE bloom_filter GRANULARITY 4,
    INDEX idx_command command TYPE tokenbf_v1(32768, 3, 0) GRANULARITY 4
)
ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_time)
ORDER BY (event_time, shell_session_id)
TTL event_time + INTERVAL 90 DAY
SETTINGS index_granularity = 8192
COMMENT '터미널 명령어 실행 이력';

-- 사용자 생성 및 권한 부여 (환경변수로 자동 생성되지만 명시적으로 권한 부여)
-- 기본 사용자: ts_dev / netcruz!#$134
GRANT ALL ON terminal_history.* TO ts_dev;

-- 샘플 쿼리 예시 (주석 처리)
-- 최근 100개 명령어 조회
-- SELECT * FROM terminal_history.terminal_command_history ORDER BY event_time DESC LIMIT 100;

-- 사용자별 명령어 통계
-- SELECT user_email, count() as cmd_count FROM terminal_history.terminal_command_history GROUP BY user_email;

-- 호스트별 명령어 통계
-- SELECT host, count() as cmd_count FROM terminal_history.terminal_command_history GROUP BY host;

-- 특정 명령어 검색
-- SELECT * FROM terminal_history.terminal_command_history WHERE command LIKE '%ls%' LIMIT 10;

-- 월별 명령어 실행 통계
-- SELECT toYYYYMM(event_time) as month, count() as cmd_count FROM terminal_history.terminal_command_history GROUP BY month ORDER BY month DESC;
