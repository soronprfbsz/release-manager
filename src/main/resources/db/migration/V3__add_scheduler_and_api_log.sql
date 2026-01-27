-- =========================================================
-- V3: 스케줄러 기능 및 API 로깅 기능 추가
-- =========================================================
-- 1. 범용 스케줄러: API URL을 등록하고 Cron 표현식에 따라 호출
-- 2. API 로깅: 모든 API 요청/응답을 기록
-- =========================================================

-- =========================================================
-- Step 1: 스케쥴러 메뉴 추가
-- =========================================================
-- 메뉴 구조: 업무지원(support) > 원격작업(remote_jobs) > 스케쥴러(remote_scheduler)

-- 3depth 메뉴 - 스케쥴러
INSERT INTO menu (menu_id, menu_name, menu_url, icon, is_icon_visible, description, is_description_visible, is_line_break, menu_order) VALUES
('remote_scheduler', '스케쥴러', 'support/remote-jobs/scheduler', 'clock', TRUE, 'API 스케줄 작업을 관리합니다.', TRUE, FALSE, 3);

-- menu_hierarchy - 자기 자신 관계 (depth=0)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('remote_scheduler', 'remote_scheduler', 0);

-- menu_hierarchy - 부모-자식 관계 (depth=1) - remote_jobs > remote_scheduler
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('remote_jobs', 'remote_scheduler', 1);

-- menu_hierarchy - 조상-손자 관계 (depth=2) - support > remote_jobs > remote_scheduler
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('support', 'remote_scheduler', 2);

-- menu_role - ADMIN, DEVELOPER, USER 권한 부여
INSERT INTO menu_role (menu_id, role) VALUES
('remote_scheduler', 'ADMIN'),
('remote_scheduler', 'DEVELOPER');

-- =========================================================
-- Step 2: 스케줄 작업 테이블 생성
-- =========================================================

-- ---------------------------------------------------------
-- 스케줄 작업 정의
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule_job (
    job_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '작업 ID',
    job_name VARCHAR(100) NOT NULL COMMENT '작업명 (고유)',
    job_group VARCHAR(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '작업 그룹',
    description VARCHAR(500) COMMENT '작업 설명',

    -- API 호출 정보
    api_url VARCHAR(500) NOT NULL COMMENT '호출할 API URL',
    http_method VARCHAR(10) NOT NULL DEFAULT 'POST' COMMENT 'HTTP 메서드 (GET, POST, PUT, DELETE)',
    request_body TEXT COMMENT '요청 본문 (JSON)',
    request_headers TEXT COMMENT '요청 헤더 (JSON)',

    -- 스케줄 설정
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron 표현식',
    timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Seoul' COMMENT '타임존',

    -- 실행 설정
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    timeout_seconds INT NOT NULL DEFAULT 30 COMMENT '타임아웃 (초)',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '실패 시 재시도 횟수',
    retry_delay_seconds INT NOT NULL DEFAULT 5 COMMENT '재시도 간격 (초)',

    -- 메타 정보
    last_executed_at DATETIME COMMENT '마지막 실행 시각',
    next_execution_at DATETIME COMMENT '다음 실행 예정 시각',
    created_by BIGINT COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    UNIQUE INDEX idx_sj_job_name (job_name),
    INDEX idx_sj_job_group (job_group),
    INDEX idx_sj_is_enabled (is_enabled),
    INDEX idx_sj_next_execution (next_execution_at),

    CONSTRAINT fk_schedule_job_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스케줄 작업 정의';

-- ---------------------------------------------------------
-- 스케줄 실행 이력
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS schedule_job_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 ID',
    job_id BIGINT NOT NULL COMMENT '작업 ID',
    job_name VARCHAR(100) NOT NULL COMMENT '작업명 (스냅샷)',

    -- 실행 정보
    started_at DATETIME NOT NULL COMMENT '시작 시각',
    finished_at DATETIME COMMENT '종료 시각',
    execution_time_ms BIGINT COMMENT '실행 시간 (밀리초)',

    -- 결과 정보
    status VARCHAR(20) NOT NULL COMMENT '상태 (RUNNING, SUCCESS, FAILED, TIMEOUT)',
    response_code INT COMMENT 'HTTP 응답 코드',
    response_body TEXT COMMENT '응답 본문',
    error_message TEXT COMMENT '에러 메시지',

    -- 재시도 정보
    attempt_number INT NOT NULL DEFAULT 1 COMMENT '시도 횟수',

    INDEX idx_sjh_job_id (job_id),
    INDEX idx_sjh_started_at (started_at),
    INDEX idx_sjh_status (status),
    INDEX idx_sjh_job_started (job_id, started_at DESC),

    CONSTRAINT fk_schedule_job_history_job FOREIGN KEY (job_id)
        REFERENCES schedule_job(job_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='스케줄 실행 이력';

-- =========================================================
-- 초기 스케줄 작업 데이터 (내부 유지보수 작업)
-- =========================================================

-- 유령 이미지 정리 (매일 새벽 3시)
-- api_url의 {port}는 스케줄러 실행 시 server.port 값으로 치환됨
INSERT INTO schedule_job (job_name, job_group, description, api_url, http_method, cron_expression, timeout_seconds) VALUES
('board-image-cleanup', 'MAINTENANCE', '게시판 유령 이미지 정리 (24시간 이상 미사용)',
 'http://localhost:{port}/api/maintenance/board-images', 'DELETE',
 '0 0 3 * * *', 60);

-- 오래된 스케줄 실행 이력 정리 (매주 일요일 새벽 4시)
INSERT INTO schedule_job (job_name, job_group, description, api_url, http_method, cron_expression, timeout_seconds) VALUES
('schedule-history-cleanup', 'MAINTENANCE', '90일 이상 지난 스케줄 실행 이력 정리',
 'http://localhost:{port}/api/maintenance/schedule-histories', 'DELETE',
 '0 0 4 * * SUN', 60);

-- =========================================================
-- Step 3: API 로깅 테이블 생성
-- =========================================================
-- 모든 API 요청/응답을 기록하여 디버깅 및 감사 추적에 활용
-- =========================================================

CREATE TABLE IF NOT EXISTS api_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '로그 ID',

    -- 요청 정보
    request_id VARCHAR(36) NOT NULL COMMENT '요청 고유 ID (UUID)',
    http_method VARCHAR(10) NOT NULL COMMENT 'HTTP 메서드',
    request_uri VARCHAR(500) NOT NULL COMMENT '요청 URI',
    query_string VARCHAR(2000) COMMENT '쿼리스트링',
    request_body TEXT COMMENT '요청 본문 (JSON, 최대 10KB)',
    request_content_type VARCHAR(100) COMMENT '요청 Content-Type',

    -- 응답 정보
    response_status INT NOT NULL COMMENT 'HTTP 상태 코드',
    response_body TEXT COMMENT '응답 본문 (JSON, 최대 10KB)',
    response_content_type VARCHAR(100) COMMENT '응답 Content-Type',

    -- 클라이언트 정보
    client_ip VARCHAR(45) COMMENT '클라이언트 IP (IPv6 지원)',
    user_agent VARCHAR(500) COMMENT 'User-Agent',

    -- 사용자 정보
    account_id BIGINT COMMENT '요청자 계정 ID',
    account_email VARCHAR(100) COMMENT '요청자 이메일',

    -- 성능 정보
    execution_time_ms BIGINT COMMENT '처리 시간 (밀리초)',

    -- 타임스탬프
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    INDEX idx_al_created_at (created_at),
    INDEX idx_al_request_uri (request_uri(100)),
    INDEX idx_al_http_method (http_method),
    INDEX idx_al_response_status (response_status),
    INDEX idx_al_account_id (account_id),
    INDEX idx_al_request_id (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API 요청/응답 로그';

-- API 로그 정리 스케줄 (매주 일요일 새벽 5시)
INSERT INTO schedule_job (job_name, job_group, description, api_url, http_method, cron_expression, timeout_seconds) VALUES
('api-log-cleanup', 'MAINTENANCE', '30일 이상 지난 API 로그 정리',
 'http://localhost:{port}/api/maintenance/api-logs', 'DELETE',
 '0 0 5 * * SUN', 120);

-- =========================================================
-- Step 4: 운영 이력 메뉴 추가
-- =========================================================
-- 메뉴 구조: 운영관리(operation_management) > 운영 이력(operation_history)
-- 접근 권한: ADMIN, DEVELOPER

-- 2depth 메뉴 - 운영 이력
INSERT INTO menu (menu_id, menu_name, menu_url, icon, is_icon_visible, description, is_description_visible, is_line_break, menu_order) VALUES
('operation_history', '운영 이력', 'operations/history', 'history', TRUE, 'API 실행 이력을 조회합니다.', TRUE, FALSE, 6);

-- menu_hierarchy - 자기 자신 관계 (depth=0)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('operation_history', 'operation_history', 0);

-- menu_hierarchy - 부모-자식 관계 (depth=1) - operation_management > operation_history
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('operation_management', 'operation_history', 1);

-- menu_role - ADMIN, DEVELOPER 권한만 부여
INSERT INTO menu_role (menu_id, role) VALUES
('operation_history', 'ADMIN'),
('operation_history', 'DEVELOPER');
