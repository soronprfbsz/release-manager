-- =========================================================
-- V1: Release Manager 통합 초기화 스크립트
-- 작성일: 2025-12-04
-- 작성자: Claude Code
-- 설명: 모든 테이블 및 초기 데이터 생성
-- =========================================================

-- =========================================================
-- Section 1: Code 시스템 테이블 생성
-- =========================================================

-- code_type 테이블 생성
CREATE TABLE IF NOT EXISTS code_type (
    code_type_id VARCHAR(50) PRIMARY KEY COMMENT '코드 타입 ID',
    code_type_name VARCHAR(100) NOT NULL COMMENT '코드 타입 명',
    description VARCHAR(200) COMMENT '설명',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_code_type_name (code_type_name),
    INDEX idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='코드 타입 테이블';

-- code 테이블 생성
CREATE TABLE IF NOT EXISTS code (
    code_type_id VARCHAR(50) NOT NULL COMMENT '코드 타입',
    code_id VARCHAR(100) NOT NULL COMMENT '코드 ID',
    code_name VARCHAR(100) NOT NULL COMMENT '코드 명',
    description VARCHAR(200) COMMENT '설명',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    PRIMARY KEY (code_type_id, code_id),
    INDEX idx_code_type_id (code_type_id),
    INDEX idx_is_enabled (is_enabled),
    CONSTRAINT fk_code_type FOREIGN KEY (code_type_id) REFERENCES code_type(code_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='코드 테이블';

-- =========================================================
-- Section 2: Account 테이블 생성
-- =========================================================

-- account 테이블 생성
CREATE TABLE IF NOT EXISTS account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '계정 ID',
    account_name VARCHAR(50) NOT NULL COMMENT '계정 이름',
    email VARCHAR(50) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(100) NOT NULL COMMENT '비밀번호',
    role VARCHAR(100) NOT NULL COMMENT '계정 권한',
    status VARCHAR(100) NOT NULL COMMENT '계정 상태',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계정 테이블';

-- =========================================================
-- Section 3: Customer 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고객사 ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '고객사 코드',
    customer_name VARCHAR(100) NOT NULL COMMENT '고객사 명',
    description VARCHAR(255) COMMENT '설명',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    updated_by VARCHAR(100) NOT NULL COMMENT '수정자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_name (customer_name),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='고객사 테이블';

-- =========================================================
-- Section 4: Release Version 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS release_version (
    release_version_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 버전 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    release_category VARCHAR(20) NOT NULL DEFAULT 'PATCH' COMMENT '릴리즈 카테고리 (INSTALL/PATCH)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 릴리즈인 경우)',
    version VARCHAR(50) NOT NULL UNIQUE COMMENT '버전 번호 (예: 1.1.0)',
    major_version INT NOT NULL COMMENT '메이저 버전',
    minor_version INT NOT NULL COMMENT '마이너 버전',
    patch_version INT NOT NULL COMMENT '패치 버전',
    custom_version VARCHAR(100) COMMENT '커스텀 버전',
    comment TEXT COMMENT '버전 설명',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    INDEX idx_release_type (release_type),
    INDEX idx_release_category (release_category),
    INDEX idx_customer_id (customer_id),
    INDEX idx_version (version),
    INDEX idx_major_minor (major_version, minor_version),
    INDEX idx_created_at (created_at),

    CONSTRAINT fk_release_version_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 버전 테이블';

-- =========================================================
-- Section 5: Release File 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS release_file (
    release_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 파일 ID',
    release_version_id BIGINT NOT NULL COMMENT '릴리즈 버전 ID',
    file_type VARCHAR(50) NOT NULL COMMENT '파일 타입 (SQL, PDF, MD, EXE, SH, TXT)',
    file_category VARCHAR(50) COMMENT '파일 카테고리 (DATABASE, WEB, ENGINE, ETC)',
    sub_category VARCHAR(50) COMMENT '하위 카테고리 (MARIADB, CRATEDB, METADATA 등)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (물리 경로)',
    relative_path VARCHAR(500) COMMENT 'ZIP 파일 내부 상대 경로',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (MD5)',
    execution_order INT NOT NULL DEFAULT 1 COMMENT '실행 순서',
    description TEXT COMMENT '파일 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_release_version_id (release_version_id),
    INDEX idx_file_type (file_type),
    INDEX idx_file_category (file_category),
    INDEX idx_sub_category (sub_category),
    INDEX idx_file_path (file_path),
    INDEX idx_execution_order (execution_order),

    CONSTRAINT fk_release_file_version FOREIGN KEY (release_version_id)
        REFERENCES release_version(release_version_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 파일 테이블';

-- =========================================================
-- Section 7: Release Version Hierarchy 클로저 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS release_version_hierarchy (
    ancestor_id BIGINT NOT NULL COMMENT '상위 버전 ID (조상)',
    descendant_id BIGINT NOT NULL COMMENT '하위 버전 ID (후손)',
    depth INT NOT NULL COMMENT '계층 깊이 (0=자기자신, 1=직접 자식, 2=손자...)',

    PRIMARY KEY (ancestor_id, descendant_id),

    INDEX idx_ancestor (ancestor_id),
    INDEX idx_descendant (descendant_id),
    INDEX idx_depth (depth),

    CONSTRAINT fk_hierarchy_ancestor FOREIGN KEY (ancestor_id)
        REFERENCES release_version(release_version_id) ON DELETE CASCADE,
    CONSTRAINT fk_hierarchy_descendant FOREIGN KEY (descendant_id)
        REFERENCES release_version(release_version_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='릴리즈 버전 계층 구조 테이블 (Closure Table)';

-- =========================================================
-- Section 8: Resource File 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS resource_file (
    resource_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '리소스 파일 ID',
    file_type VARCHAR(20) NOT NULL COMMENT '파일 타입 (확장자 대문자, 예: SH, PDF, MD)',
    file_category VARCHAR(50) NOT NULL COMMENT '파일 카테고리 (SCRIPT, DOCUMENT, ETC)',
    sub_category VARCHAR(50) COMMENT '하위 카테고리',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (resource/ 하위 상대경로)',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (SHA-256)',
    description TEXT COMMENT '파일 설명',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_rf_file_type (file_type),
    INDEX idx_rf_file_category (file_category),
    INDEX idx_rf_sub_category (sub_category),
    INDEX idx_rf_file_name (file_name),
    INDEX idx_rf_file_path (file_path),
    INDEX idx_rf_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='리소스 파일 테이블';

-- =========================================================
-- Section 9: Backup File 테이블 생성 (Job 도메인)
-- =========================================================

CREATE TABLE IF NOT EXISTS backup_file (
    backup_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '백업 파일 ID',
    file_category VARCHAR(20) NOT NULL COMMENT '파일 카테고리 (MARIADB, CRATEDB)',
    file_type VARCHAR(20) NOT NULL COMMENT '파일 타입 (확장자 대문자, 예: SQL)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (job/backup_files/ 하위 상대경로)',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (SHA-256)',
    description TEXT COMMENT '파일 설명',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bf_file_category (file_category),
    INDEX idx_bf_file_type (file_type),
    INDEX idx_bf_file_name (file_name),
    INDEX idx_bf_file_path (file_path),
    INDEX idx_bf_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='백업 파일 테이블';

-- =========================================================
-- Section 10: Department 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS department (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '부서 ID',
    department_name VARCHAR(100) NOT NULL UNIQUE COMMENT '부서명',
    description VARCHAR(500) COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_department_name (department_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서 테이블';

-- =========================================================
-- Section 11: Engineer 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS engineer (
    engineer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '엔지니어 ID',
    engineer_name VARCHAR(50) NOT NULL COMMENT '엔지니어 이름',
    engineer_email VARCHAR(100) NOT NULL UNIQUE COMMENT '엔지니어 회사 이메일',
    engineer_phone VARCHAR(20) COMMENT '엔지니어 연락처',
    position VARCHAR(100) COMMENT '직급 (code_type_id=POSITION 참조)',
    department_id BIGINT COMMENT '소속 부서 ID',
    description VARCHAR(500) COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    created_by VARCHAR(100) DEFAULT 'SYSTEM' NOT NULL COMMENT '생성자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM' NOT NULL COMMENT '수정자',

    INDEX idx_engineer_name (engineer_name),
    INDEX idx_engineer_email (engineer_email),
    INDEX idx_engineer_position (position),
    INDEX idx_department_id (department_id),

    CONSTRAINT fk_engineer_department FOREIGN KEY (department_id)
        REFERENCES department(department_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='엔지니어 테이블';

-- =========================================================
-- Section 11: 코드 타입 기본 데이터
-- =========================================================

INSERT INTO code_type (code_type_id, code_type_name, description) VALUES
('ACCOUNT_ROLE', '계정 권한', '계정 권한 구분'),
('ACCOUNT_STATUS', '계정 상태', '계정 상태 구분'),
('RELEASE_TYPE', '릴리즈 타입', '릴리즈 타입 구분 (표준/커스텀)'),
('RELEASE_CATEGORY', '릴리즈 카테고리', '릴리즈 카테고리 구분 (설치본/패치본)'),
('DATABASE_TYPE', '데이터베이스 타입', '지원하는 데이터베이스 종류'),
('FILE_TYPE', '파일 타입', '파일 확장자 타입'),
('FILE_CATEGORY', '파일 카테고리', '파일 기능적 대분류'),
('FILE_SUBCATEGORY_DATABASE', '데이터베이스 파일 서브 카테고리', 'DATABASE 카테고리 소분류'),
('FILE_SUBCATEGORY_ENGINE', '엔진 파일 서브 카테고리', 'ENGINE 카테고리 소분류'),
('RESOURCE_FILE_CATEGORY', '리소스 파일 카테고리', '리소스 파일 기능적 대분류'),
('RESOURCE_SUBCATEGORY_SCRIPT', '스크립트 서브 카테고리', 'SCRIPT 카테고리 소분류'),
('RESOURCE_SUBCATEGORY_DOCUMENT', '문서 서브 카테고리', 'DOCUMENT 카테고리 소분류'),
('BACKUP_FILE_CATEGORY', '백업 파일 카테고리', '백업 파일 분류 (MARIADB, CRATEDB)'),
('POSITION', '직급', '엔지니어 직급 구분');

-- =========================================================
-- Section 11: 계정 권한 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order) VALUES
('ACCOUNT_ROLE', 'ADMIN', '관리자', '시스템 관리자 권한', 1),
('ACCOUNT_ROLE', 'USER', '일반 사용자', '일반 사용자 권한', 2),
('ACCOUNT_ROLE', 'GUEST', '게스트', '게스트 사용자 권한', 3);

-- =========================================================
-- Section 12: 계정 상태 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order) VALUES
('ACCOUNT_STATUS', 'ACTIVE', '활성', '활성 상태', 1),
('ACCOUNT_STATUS', 'INACTIVE', '비활성', '비활성 상태', 2),
('ACCOUNT_STATUS', 'SUSPENDED', '정지', '정지 상태', 3);

-- =========================================================
-- Section 13: 릴리즈 타입 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RELEASE_TYPE', 'STANDARD', '표준 릴리즈', '모든 고객사 공통 적용 릴리즈', 1, TRUE),
('RELEASE_TYPE', 'CUSTOM', '커스텀 릴리즈', '특정 고객사 전용 릴리즈', 2, TRUE);

-- =========================================================
-- Section 14: 릴리즈 카테고리 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RELEASE_CATEGORY', 'INSTALL', '설치본', '최초 설치용 릴리즈', 1, TRUE),
('RELEASE_CATEGORY', 'PATCH', '패치본', '업데이트용 패치 릴리즈', 2, TRUE);

-- =========================================================
-- Section 15: 데이터베이스 타입 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('DATABASE_TYPE', 'MARIADB', 'MariaDB', 'MariaDB 데이터베이스', 1, TRUE),
('DATABASE_TYPE', 'CRATEDB', 'CrateDB', 'CrateDB 데이터베이스', 2, TRUE);

-- =========================================================
-- Section 16: 파일 타입 코드 (확장자)
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_TYPE', 'SQL', 'SQL', 'SQL 스크립트 파일', 1, TRUE),
('FILE_TYPE', 'MD', 'MD', '마크다운 문서 파일', 2, TRUE),
('FILE_TYPE', 'PDF', 'PDF', 'PDF 문서 파일', 3, TRUE),
('FILE_TYPE', 'EXE', 'EXE', '실행 파일', 4, TRUE),
('FILE_TYPE', 'SH', 'SH', '쉘 스크립트 파일', 5, TRUE),
('FILE_TYPE', 'TXT', 'TXT', '텍스트 파일', 6, TRUE),
('FILE_TYPE', 'JAR', 'JAR', 'Java Archive 파일', 7, TRUE),
('FILE_TYPE', 'WAR', 'WAR', 'Web Archive 파일', 8, TRUE),
('FILE_TYPE', 'TAR', 'TAR', 'TAR 압축 파일', 9, TRUE),
('FILE_TYPE', 'GZ', 'GZ', 'GZIP 압축 파일', 10, TRUE),
('FILE_TYPE', 'ZIP', 'ZIP', 'ZIP 압축 파일', 11, TRUE),
('FILE_TYPE', 'UNDEFINED', 'UNDEFINED', '정의되지 않은 파일 타입', 99, TRUE);

-- =========================================================
-- Section 17: 파일 카테고리 코드 (기능적 대분류)
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_CATEGORY', 'DATABASE', 'DATABASE', '데이터베이스 관련 파일', 1, TRUE),
('FILE_CATEGORY', 'WEB', 'WEB', '웹 애플리케이션 파일', 2, TRUE),
('FILE_CATEGORY', 'ENGINE', 'ENGINE', '엔진 관련 파일', 3, TRUE),
('FILE_CATEGORY', 'ETC', 'ETC', '기타 파일', 4, TRUE);

-- =========================================================
-- Section 18: 파일 서브 카테고리 코드 (카테고리별 소분류)
-- =========================================================

-- DATABASE 서브 카테고리
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY_DATABASE', 'CRATEDB', 'CRATEDB', 'CrateDB 스크립트', 1, TRUE),
('FILE_SUBCATEGORY_DATABASE', 'MARIADB', 'MARIADB', 'MariaDB 스크립트', 2, TRUE),
('FILE_SUBCATEGORY_DATABASE', 'ETC', 'ETC', '기타 파일', 3, TRUE);

-- ENGINE 서브 카테고리
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY_ENGINE', 'NC_AI_EVENT', 'NC_AI_EVENT', '', 1, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_AI_LEARN', 'NC_AI_LEARN', '', 2, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_AI_MGR', 'NC_AI_MGR', '', 3, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_AP', 'NC_AP', '무선 AP 구성, 수집, 알재비 이벤트 발생', 4, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_API_AP', 'NC_API_AP', 'api 연동으로 무선AP 수집', 5, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_API_KAL', 'NC_API_KAL', '대한항공 api 연동 엔진', 6, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_ARP', 'NC_ARP', 'ARP 정보 수집 엔진_NC_CUSTOM의 브릿지엔진(ARP_MAC_SCAN)', 7, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CONF', 'NC_CONF', '장비 관리', 8, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CONFIBACK', 'NC_CONFIBACK', '각 장비에서 커맨드 또는 스크립트 주기적으로 실행', 9, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CPPM_CHK', 'NC_CPPM_CHK', '삼성전자 CPPM 체크 엔진', 10, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CUSTOM', 'NC_CUSTOM', '사이트 별 커스텀 가능한 필요한 정우 사용', 11, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_DB_MIG', 'NC_DB_MIG', 'DB 마이그레이션 엔진', 12, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_DPI_KUMOH', 'NC_DPI_KUMOH', '금오공대 DPI엔동 엔진', 13, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EMS', 'NC_EMS', '', 14, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EVENT_SENDER', 'NC_EVENT_SENDER', '외부 이벤트 연동 엔진 개발', 15, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EVENTPUSHER', 'NC_EVENTPUSHER', '', 16, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EXEC', 'NC_EXEC', '', 17, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FAULT_CP', 'NC_FAULT_CP', '', 18, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FAULT_EX', 'NC_FAULT_EX', '', 19, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FAULT_MS', 'NC_FAULT_MS', '각 엔진의 이벤트를 발생/복구 처리', 20, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FMS', 'NC_FMS', '설비 장비를 대상으로 구성정보와 성능정보를 수집', 21, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_HTTP_SVR', 'NC_HTTP_SVR', 'http listen server 엔진. 수신 시 설정에 따른 이벤트 처리', 22, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPSLA', 'NC_IPSLA', '', 23, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPT', 'NC_IPT', '', 24, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPT_CDR', 'NC_IPT_CDR', '', 25, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPTMAC', 'NC_IPTMAC', '', 26, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_KNB', 'NC_KNB', '', 27, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_L4', 'NC_L4', 'L4 엔진 로그 및 XML 분석', 28, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_L4LB', 'NC_L4LB', '', 29, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_L7', 'NC_L7', 'L7 수집 엔진', 30, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_MIB_PARSER', 'NC_MIB_PARSER', '', 31, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NAC_KUMOH', 'NC_NAC_KUMOH', '금오공대 아상전우 연동 엔진(NAC)', 32, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NDI', 'NC_NDI', '', 33, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NET_SCAN', 'NC_NET_SCAN', '', 34, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NOTI', 'NC_NOTI', '이벤트 알림', 35, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NOTI_TCPCLIENT', 'NC_NOTI_TCPCLIENT', 'NC_NOTI TCP 연동을 위한 엔진', 36, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_PACKET', 'NC_PACKET', '', 37, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_PERF', 'NC_PERF', '성능 데이터 수집', 38, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_PERF_LEARN', 'NC_PERF_LEARN', '', 39, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REPEAT_EVENT', 'NC_REPEAT_EVENT', '메리츠증권 반복장애 이벤트 처리 엔진', 40, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REQUEST_URL', 'NC_REQUEST_URL', '', 41, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REQUEST_URL_NOKIA', 'NC_REQUEST_URL_NOKIA', 'nokia kafka 연동 이벤트처리 엔진', 42, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REST_API', 'NC_REST_API', '', 43, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_RT_TOOL', 'NC_RT_TOOL', '동록된 장치에 ICMP, SNMP를 요청 로그를 수집', 44, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_RTT_CLI', 'NC_RTT_CLI', '', 45, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SAMPLE', 'NC_SAMPLE', '', 46, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SDN', 'NC_SDN', '', 47, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SFLOW_C', 'NC_SFLOW_C', '', 48, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SLB', 'NC_SLB', 'ssh/telnet 접속하여 L4 명령어 기반 수집 엔진', 49, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SMS', 'NC_SMS', '서버 구성정보 및 성능정보를 수집', 50, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_Snmp', 'NC_Snmp', '', 51, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SNMP3_CHK', 'NC_SNMP3_CHK', 'snmpv3 engine_id 체크 엔진', 52, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SVC_CHK', 'NC_SVC_CHK', 'Port 및 URL 체크', 53, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SYSTRAP', 'NC_SYSTRAP', 'syslog와 snmp trap 로그 수집 및 이벤트 발생', 54, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_TMS', 'NC_TMS', 'TMS engine', 55, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_TRACERT', 'NC_TRACERT', '', 56, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_UI_CP', 'NC_UI_CP', 'UI에서 요청하는 command와 snmp 명령을 수행하여 결과를 리턴', 57, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_UPS', 'NC_UPS', 'UPS 장비의 정보 및 상태 확인', 58, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_UTIL', 'NC_UTIL', '', 59, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_VMM', 'NC_VMM', '', 60, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_VPN', 'NC_VPN', 'ssh/telnet 접속하여 터널정보를 수집', 61, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_WATCHDOG', 'NC_WATCHDOG', 'NMS 엔진 관리 및 스케줄 작업 관리 수행', 62, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_X25', 'NC_X25', '', 63, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'OZ_CCTV', 'OZ_CCTV', '', 64, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'ETC', 'ETC', '기타 파일', 65, TRUE);

-- =========================================================
-- Section 19: 리소스 파일 카테고리 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_FILE_CATEGORY', 'SCRIPT', '스크립트', '스크립트 파일', 1, TRUE),
('RESOURCE_FILE_CATEGORY', 'DOCUMENT', '문서', '문서 파일', 2, TRUE),
('RESOURCE_FILE_CATEGORY', 'ETC', '기타', '기타 파일', 3, TRUE);

-- =========================================================
-- Section 20: 리소스 파일 서브카테고리 - SCRIPT
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_SUBCATEGORY_SCRIPT', 'MARIADB_BACKUP', 'MARIADB 백업', 'MariaDB 백업 스크립트', 1, TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', 'MARIADB_RESTORE', 'MARIADB 복원', 'MariaDB 복원 스크립트', 2, TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', 'CRATEDB_BACKUP', 'CRATEDB 백업', 'CrateDB 백업 스크립트', 3, TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', 'CRATEDB_RESTORE', 'CRATEDB 복원', 'CrateDB 복원 스크립트', 4, TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', 'ETC', '기타', '기타', 99, TRUE);

-- =========================================================
-- Section 21: 리소스 파일 서브카테고리 - DOCUMENT
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_SUBCATEGORY_DOCUMENT', 'INFRAEYE1', 'Infraeye1', 'Infraeye1 문서', 1, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'INFRAEYE2', 'Infraeye2', 'Infraeye2 문서', 2, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'DATABASE', '데이터베이스', 'DATABASE 관련 문서', 3, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'OS', 'O/S', 'O/S 관련 문서', 4, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'LANGUAGE', '언어', '언어 관련 문서', 5, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'ETC', '기타', '기타 문서', 99, TRUE);

-- =========================================================
-- Section 22: 백업 파일 카테고리 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('BACKUP_FILE_CATEGORY', 'MARIADB', 'MariaDB', 'MariaDB 백업 파일', 1, TRUE),
('BACKUP_FILE_CATEGORY', 'CRATEDB', 'CrateDB', 'CrateDB 백업 파일', 2, TRUE);

-- =========================================================
-- Section 22-1: 직급 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('POSITION', 'DIRECTOR', '이사', '이사', 1, TRUE),
('POSITION', 'GENERAL_MANAGER', '부장', '부장', 2, TRUE),
('POSITION', 'DEPUTY_MANAGER', '차장', '차장', 3, TRUE),
('POSITION', 'MANAGER', '과장', '과장', 4, TRUE),
('POSITION', 'ASSISTANT_MANAGER', '대리', '대리', 5, TRUE),
('POSITION', 'STAFF', '사원', '사원', 6, TRUE);

-- =========================================================
-- Section 23: 기본 관리자 계정
-- 패스워드: nms12345! (BCrypt 암호화)
-- =========================================================

INSERT INTO account (account_name, email, password, role, status) VALUES
('시스템 관리자','admin@tscientific.co.kr', '$2a$10$l8sMjsX460lFokTzvBuBOefMU0u//xpEzNCV4uhLvr0huqUWpTYPe', 'ADMIN', 'ACTIVE'),
('기본 사용자','m_user@tscientific.co.kr', '$2a$10$l8sMjsX460lFokTzvBuBOefMU0u//xpEzNCV4uhLvr0huqUWpTYPe', 'USER', 'ACTIVE');



-- =========================================================
-- Section 28: 부서 기본 데이터
-- =========================================================

INSERT INTO department (department_name, description) VALUES
('인프라기술팀', '인프라 기술 지원'),
('서비스기술팀', '서비스 기술 지원'),
('보안기술팀', '보안 기술 지원');


-- =========================================================
-- Section 29: 엔지니어 기본 데이터
-- =========================================================

INSERT INTO engineer (engineer_email, position, engineer_name, department_id)
VALUES ('shinss@tscientific.co.kr', '부장','신성수', 1),
('yhkim0144@tscientific.co.kr', '과장','김요한', 1),
('skykimtw@tscientific.co.kr', '과장','김태우', 1),
('choi7733@tscientific.co.kr', '과장','최은빈', 1),
('thdrudcks97@tscientific.co.kr', '대리','송경찬', 1),
('swngh56@tscientific.co.kr', '사원','신주호', 1),
('tjddyd3050@tscientific.co.kr', '사원', '최성용', 1),
('yeonhyuck@tscientific.co.kr', '사원', '최연혁', 1),
('yoonss@tscientific.co.kr', '이사', '윤성식', 2),
('eu@tscientific.co.kr', '대리', '은지영', 2),
('wychoi@tscientific.co.kr', '대리', '윤성식', 2),
('kchh3617@tscientific.co.kr', '대리', '권찬혁', 2),
('swoun4221@tscientific.co.kr', '사원', '신운성', 2),
('rlaud95@tscientific.co.kr', '사원', '손기명', 2),
('ljk0105@tscientific.co.kr', '사원', '이정규', 2),
('jmkim@tscientific.co.kr', '차장', '김정목', 3),
('ssyang9417@tscientific.co.kr', '과장', '송병준', 3),
('dydtls888@tscientific.co.kr', '사원', '박용신', 3),
('twins827@tscientific.co.kr', '사원', '박준성', 3),
('tngur317@tscientific.co.kr', '사원', '오수혁', 3),
('oyj961212@tscientific.co.kr', '사원', '오유준', 3),
('yoonchul.lee@tscientific.co.kr', '사원', '이윤철', 3),
('lkh2433@tscientific.co.kr', '사원', '이경호', 3),
('pjh@tscientific.co.kr', '사원', '박재호', 3);


-- =========================================================
-- Section 30: 고객사 추가 (테스트용)
-- =========================================================
-- 고객사 테스트 데이터 INSERT (A~Z)
INSERT INTO customer (created_by, customer_code, customer_name, description, is_active, updated_by) VALUES
('m_user@tscientific.co.kr', 'customerA', 'A회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerB', 'B회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerC', 'C회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerD', 'D회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerE', 'E회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerF', 'F회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerG', 'G회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerH', 'H회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerI', 'I회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerJ', 'J회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerK', 'K회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerL', 'L회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerM', 'M회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerN', 'N회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerO', 'O회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerP', 'P회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerQ', 'Q회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerR', 'R회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerS', 'S회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerT', 'T회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerU', 'U회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerV', 'V회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerW', 'W회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerX', 'X회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerY', 'Y회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerZ', 'Z회사', NULL, true, 'm_user@tscientific.co.kr');


-- =========================================================
-- Section 6: Cumulative Patch 테이블 생성
-- =========================================================

CREATE TABLE IF NOT EXISTS cumulative_patch (
    patch_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '패치 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 패치인 경우)',
    from_version VARCHAR(50) NOT NULL COMMENT '시작 버전',
    to_version VARCHAR(50) NOT NULL COMMENT '종료 버전',
    patch_name VARCHAR(100) NOT NULL COMMENT '패치 파일명',
    output_path VARCHAR(500) NOT NULL COMMENT '생성된 패치 파일 경로',
    description TEXT COMMENT '설명',
    engineer_id BIGINT COMMENT '패치 담당자 (엔지니어 ID)',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_cp_release_type (release_type),
    INDEX idx_cp_customer_id (customer_id),
    INDEX idx_cp_from_version (from_version),
    INDEX idx_cp_to_version (to_version),
    INDEX idx_cp_engineer_id (engineer_id),
    INDEX idx_cp_created_at (created_at),

    CONSTRAINT fk_cumulative_patch_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_cumulative_patch_engineer FOREIGN KEY (engineer_id)
        REFERENCES engineer(engineer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='누적 패치 테이블';


-- =========================================================
-- Section 24: 릴리즈 버전 데이터
-- =========================================================

INSERT INTO release_version (
    release_type, release_category, customer_id, version,
    major_version, minor_version, patch_version,
    created_by, comment, created_at
) VALUES
('STANDARD', 'INSTALL', NULL, '1.0.0', 1, 0, 0, 'TS', '최초 설치본', '2025-01-01 00:00:00'),
('STANDARD', 'PATCH', NULL, '1.1.0', 1, 1, 0, 'jhlee@tscientific', '데이터코드, 이벤트코드, 메뉴코드 추가 / SMS 기능 추가 / VERSION_HISTORY 테이블 추가 / V_INFO_MCH 관련 뷰 변경', '2025-10-31 00:00:00'),
('STANDARD', 'PATCH', NULL, '1.1.1', 1, 1, 1, 'jhlee@tscientific', 'SMS - 운영관리 - 파일 기능 관련 테이블 추가', '2025-11-05 00:00:00'),
('STANDARD', 'PATCH', NULL, '1.1.2', 1, 1, 2, 'jhlee@tscientific', 'SMS - 로그관리 - 로그 모니터 정책 상세 테이블 추가', '2025-11-25 00:00:00');

-- =========================================================
-- Section 25: 릴리즈 파일 데이터
-- =========================================================

INSERT INTO release_file (
    release_version_id, file_type, file_category, sub_category, file_name, file_path, relative_path,
    file_size, checksum, execution_order, description
) VALUES
-- 1.0.0 (release_version_id = 1) - Install Documents
(1, 'PDF', 'ETC', NULL, 'Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',
    'versions/standard/1.0.x/1.0.0/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',
    '/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',
    2727778, '4e641f7d25bbaa0061f553b92ef3d9e9', 1, '설치 가이드 문서'),
(1, 'MD', 'ETC', NULL, '설치본정보.md',
    'versions/standard/1.0.x/1.0.0/install/설치본정보.md',
    '/install/설치본정보.md',
    778, '8e5adf2b877090de4f3ec5739f71216c', 2, '설치본 정보'),

-- 1.1.0 (release_version_id = 2) - MariaDB
(2, 'SQL', 'DATABASE', 'MARIADB', '1.patch_mariadb_ddl.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/1.patch_mariadb_ddl.sql',
    '/database/MARIADB/1.patch_mariadb_ddl.sql',
    34879, 'f8b9f64345555c9a4a9c9101aaa8b701', 1, 'DDL 변경'),
(2, 'SQL', 'DATABASE', 'MARIADB', '2.patch_mariadb_view.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/2.patch_mariadb_view.sql',
    '/database/MARIADB/2.patch_mariadb_view.sql',
    10742, '6735c7267bedc684f155ce05eaa5b7df', 2, 'View 변경'),
(2, 'SQL', 'DATABASE', 'MARIADB', '3.patch_mariadb_데이터코드.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/3.patch_mariadb_데이터코드.sql',
    '/database/MARIADB/3.patch_mariadb_데이터코드.sql',
    134540, 'faec479bf1582dfb20199fdd468676f7', 3, '데이터 코드 추가'),
(2, 'SQL', 'DATABASE', 'MARIADB', '4.patch_mariadb_이벤트코드.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/4.patch_mariadb_이벤트코드.sql',
    '/database/MARIADB/4.patch_mariadb_이벤트코드.sql',
    36847, 'e2e818dfa626c93894b5774badee0219', 4, '이벤트 코드 추가'),
(2, 'SQL', 'DATABASE', 'MARIADB', '5.patch_mariadb_메뉴코드.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/5.patch_mariadb_메뉴코드.sql',
    '/database/MARIADB/5.patch_mariadb_메뉴코드.sql',
    25144, '3eb290c91cf66dacbc02a746bec2bef0', 5, '메뉴 코드 추가'),
(2, 'SQL', 'DATABASE', 'MARIADB', '6.patch_mariadb_procedure.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/6.patch_mariadb_procedure.sql',
    '/database/MARIADB/6.patch_mariadb_procedure.sql',
    22183, '25942f2c2201629efcc333278f8eac38', 6, 'Procedure 변경'),
(2, 'SQL', 'DATABASE', 'MARIADB', '7.patch_mariadb_dml.sql',
    'versions/standard/1.1.x/1.1.0/database/MARIADB/7.patch_mariadb_dml.sql',
    '/database/MARIADB/7.patch_mariadb_dml.sql',
    37330, '3fa1ec88b5a638fb6d67a41119d61854', 7, 'DML 변경'),

-- 1.1.0 (release_version_id = 2) - CrateDB
(2, 'SQL', 'DATABASE', 'CRATEDB', '1.patch_cratedb_ddl.sql',
    'versions/standard/1.1.x/1.1.0/database/CRATEDB/1.patch_cratedb_ddl.sql',
    '/database/CRATEDB/1.patch_cratedb_ddl.sql',
    19363, '1b68614d70c52cade269e5accca724d5', 1, 'CrateDB DDL 변경'),

-- 1.1.1 (release_version_id = 3) - MariaDB
(3, 'SQL', 'DATABASE', 'MARIADB', '1.patch_mariadb_ddl.sql',
    'versions/standard/1.1.x/1.1.1/database/MARIADB/1.patch_mariadb_ddl.sql',
    '/database/MARIADB/1.patch_mariadb_ddl.sql',
    4867, '848ecec66ce257e0fcec4088294c816d', 1, '파일 기능 관련 DDL 추가'),
(3, 'SQL', 'DATABASE', 'MARIADB', '2.patch_mariadb_dml.sql',
    'versions/standard/1.1.x/1.1.1/database/MARIADB/2.patch_mariadb_dml.sql',
    '/database/MARIADB/2.patch_mariadb_dml.sql',
    660, '63fe833edd62599db2ce8c758eae0240', 2, '파일 기능 관련 DML 추가'),

-- 1.1.2 (release_version_id = 4) - MariaDB
(4, 'SQL', 'DATABASE', 'MARIADB', '1.patch_mariadb_ddl.sql',
    'versions/standard/1.1.x/1.1.2/database/MARIADB/1.patch_mariadb_ddl.sql',
    '/database/MARIADB/1.patch_mariadb_ddl.sql',
    1765, '48bb04f6b3f2f4560ab42c0c37fcacbc', 1, 'SMS 로그 모니터링 정책 상세 테이블 추가');

-- =========================================================
-- Section 26: 계층 구조 데이터
-- =========================================================

-- 1.0.0 (release_version_id = 1)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (1, 1, 0);

-- 1.1.0 (release_version_id = 2)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (2, 2, 0);
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (1, 2, 1);

-- 1.1.1 (release_version_id = 3)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (3, 3, 0);
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (2, 3, 1);
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (1, 3, 2);

-- 1.1.2 (release_version_id = 4)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (4, 4, 0);
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (3, 4, 1);
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (2, 4, 2);
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES (1, 4, 3);

-- =========================================================
-- Section 27: 리소스 파일 데이터
-- =========================================================

-- MariaDB 스크립트
INSERT INTO resource_file (file_type, file_category, sub_category, file_name, file_path, file_size, description, created_by) VALUES
('SH', 'SCRIPT', 'MARIADB_BACKUP', 'mariadb_backup.sh', 'resource/script/MARIADB/mariadb_backup.sh', 11025, 'MariaDB 백업 스크립트', 'system'),
('SH', 'SCRIPT', 'MARIADB_RESTORE', 'mariadb_restore.sh', 'resource/script/MARIADB/mariadb_restore.sh', 12655, 'MariaDB 복원 스크립트', 'system');

-- CrateDB 스크립트
INSERT INTO resource_file (file_type, file_category, sub_category, file_name, file_path, file_size, description, created_by) VALUES
('SH', 'SCRIPT', 'CRATEDB_BACKUP', 'cratedb_backup.sh', 'resource/script/CRATEDB/cratedb_backup.sh', 11458, 'CrateDB 백업 스크립트', 'system'),
('SH', 'SCRIPT', 'CRATEDB_RESTORE', 'cratedb_restore.sh', 'resource/script/CRATEDB/cratedb_restore.sh', 14675, 'CrateDB 복원 스크립트', 'system');

-- 문서
INSERT INTO resource_file (file_type, file_category, sub_category, file_name, file_path, file_size, description, created_by) VALUES
('PDF', 'DOCUMENT', 'INFRAEYE2', 'Infraeye2 설치가이드(OracleLinux8.6).pdf', 'resource/document/Infraeye2 설치가이드(OracleLinux8.6).pdf', 2727778, 'Infraeye2 설치 가이드 문서', 'system');
