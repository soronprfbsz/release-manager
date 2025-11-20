-- =========================================================
-- V2: Release Manager 테이블 생성
-- 작성일: 2025-11-20
-- 작성자: Claude Code
-- 설명: 릴리즈 버전 관리, 파일 관리, 누적 패치 관리 테이블
-- =========================================================

-- =========================================================
-- Step 1: Code 테이블에 기본 데이터 추가
-- =========================================================

-- RELEASE_TYPE 코드 타입 추가
INSERT INTO code_type (code_type_id, code_type_name, description, is_enabled) VALUES
('RELEASE_TYPE', '릴리즈 타입', '릴리즈 타입 구분 (표준/커스텀)', TRUE);

-- RELEASE_TYPE 코드 값 추가
INSERT INTO code (code_id, code_type_id, code_name, description, sort_order, is_enabled) VALUES
('RELEASE_TYPE_STANDARD', 'RELEASE_TYPE', '표준 릴리즈', '모든 고객사 공통 적용 릴리즈', 1, TRUE),
('RELEASE_TYPE_CUSTOM', 'RELEASE_TYPE', '커스텀 릴리즈', '특정 고객사 전용 릴리즈', 2, TRUE);

-- DATABASE_TYPE 코드 타입 추가
INSERT INTO code_type (code_type_id, code_type_name, description, is_enabled) VALUES
('DATABASE_TYPE', '데이터베이스 타입', '지원하는 데이터베이스 종류', TRUE);

-- DATABASE_TYPE 코드 값 추가
INSERT INTO code (code_id, code_type_id, code_name, description, sort_order, is_enabled) VALUES
('DATABASE_TYPE_MARIADB', 'DATABASE_TYPE', 'MariaDB', 'MariaDB 데이터베이스', 1, TRUE),
('DATABASE_TYPE_CRATEDB', 'DATABASE_TYPE', 'CrateDB', 'CrateDB 데이터베이스', 2, TRUE);

-- =========================================================
-- Step 2: Customer 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고객사 ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '고객사 코드',
    customer_name VARCHAR(100) NOT NULL COMMENT '고객사 명',
    description VARCHAR(255) COMMENT '설명',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_name (customer_name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='고객사 테이블';

-- =========================================================
-- Step 3: Release Version 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS release_version (
    release_version_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 버전 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 릴리즈인 경우)',
    version VARCHAR(50) NOT NULL UNIQUE COMMENT '버전 번호 (예: 1.1.0)',
    major_version INT NOT NULL COMMENT '메이저 버전',
    minor_version INT NOT NULL COMMENT '마이너 버전',
    patch_version INT NOT NULL COMMENT '패치 버전',
    major_minor VARCHAR(20) NOT NULL COMMENT 'Major.Minor 버전 (예: 1.1.x)',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    comment TEXT COMMENT '버전 설명',
    custom_version VARCHAR(100) COMMENT '커스텀 버전명',
    is_install BOOLEAN NOT NULL DEFAULT FALSE COMMENT '설치 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_release_type (release_type),
    INDEX idx_customer_id (customer_id),
    INDEX idx_version (version),
    INDEX idx_major_minor (major_minor),
    INDEX idx_created_at (created_at),

    CONSTRAINT fk_release_version_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 버전 테이블';

-- =========================================================
-- Step 4: Release File 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS release_file (
    release_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 파일 ID',
    release_version_id BIGINT NOT NULL COMMENT '릴리즈 버전 ID',
    database_type VARCHAR(20) NOT NULL COMMENT '데이터베이스 타입 (MARIADB/CRATEDB)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (MD5)',
    execution_order INT NOT NULL DEFAULT 1 COMMENT '실행 순서',
    description TEXT COMMENT '파일 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_release_version_id (release_version_id),
    INDEX idx_database_type (database_type),
    INDEX idx_file_path (file_path),
    INDEX idx_execution_order (execution_order),

    CONSTRAINT fk_release_file_version FOREIGN KEY (release_version_id)
        REFERENCES release_version(release_version_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 파일 테이블';

-- =========================================================
-- Step 5: Cumulative Patch History 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS cumulative_patch_history (
    cumulative_patch_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '누적 패치 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 패치인 경우)',
    from_version VARCHAR(50) NOT NULL COMMENT '시작 버전',
    to_version VARCHAR(50) NOT NULL COMMENT '종료 버전',
    patch_name VARCHAR(100) NOT NULL COMMENT '패치 파일명',
    output_path VARCHAR(500) NOT NULL COMMENT '생성된 패치 파일 경로',
    generated_at DATETIME NOT NULL COMMENT '생성일시',
    generated_by VARCHAR(100) NOT NULL COMMENT '생성자',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '생성 상태 (SUCCESS/FAILED/IN_PROGRESS)',
    error_message TEXT COMMENT '에러 메시지',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_release_type (release_type),
    INDEX idx_customer_id (customer_id),
    INDEX idx_from_version (from_version),
    INDEX idx_to_version (to_version),
    INDEX idx_status (status),
    INDEX idx_generated_at (generated_at),

    CONSTRAINT fk_cumulative_patch_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='누적 패치 생성 이력 테이블';

-- =========================================================
-- Step 6: VERSION_HISTORY 테이블 생성
-- 패치 스크립트 실행시 자동으로 기록되는 버전 이력 테이블
-- =========================================================

CREATE TABLE IF NOT EXISTS version_history
(
    version_id           VARCHAR(20) PRIMARY KEY COMMENT '버전 ID (예: 1.1.0)',
    standard_version     VARCHAR(20) NOT NULL COMMENT '표준 버전',
    custom_version       VARCHAR(20) COMMENT '커스텀 버전',
    version_created_at   DATETIME    NOT NULL COMMENT '버전 생성일시',
    version_created_by   VARCHAR(100) NOT NULL COMMENT '버전 생성자',
    system_applied_by    VARCHAR(100) COMMENT '시스템 적용자',
    system_applied_at    DATETIME COMMENT '시스템 적용일시',
    comment              TEXT COMMENT '버전 설명',
    INDEX idx_version_history_standard_version (standard_version),
    INDEX idx_version_history_system_applied_at (system_applied_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='패치 버전 이력 테이블';

-- =========================================================
-- Step 6: 샘플 데이터 추가 (선택사항)
-- =========================================================

-- 샘플 고객사 데이터
INSERT INTO customer (customer_code, customer_name, description, is_active) VALUES
('company_a', 'A회사', '샘플 고객사 A', TRUE),
('company_b', 'B회사', '샘플 고객사 B', TRUE);

-- 샘플 릴리즈 버전 데이터 (표준 릴리즈)
INSERT INTO release_version (
    release_type, customer_id, version,
    major_version, minor_version, patch_version, major_minor,
    created_by, comment, is_install
) VALUES
('STANDARD', NULL, '1.0.0', 1, 0, 0, '1.0.x', 'admin@tscientific', '초기 릴리즈 버전', FALSE),
('STANDARD', NULL, '1.1.0', 1, 1, 0, '1.1.x', 'admin@tscientific', '기능 개선 버전', FALSE);

-- 샘플 릴리즈 파일 데이터
INSERT INTO release_file (
    release_version_id, database_type, file_name, file_path,
    file_size, checksum, execution_order, description
) VALUES
(1, 'MARIADB', '001_init_schema.sql', '/release/1.0.0/mariadb/001_init_schema.sql',
 2048, 'a1b2c3d4e5f6', 1, '초기 스키마 생성'),
(1, 'MARIADB', '002_init_data.sql', '/release/1.0.0/mariadb/002_init_data.sql',
 1024, 'f6e5d4c3b2a1', 2, '초기 데이터 등록');
