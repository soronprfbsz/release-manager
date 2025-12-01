-- =========================================================
-- V1: Release Manager 통합 초기화 스크립트
-- 작성일: 2025-11-25
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
    customer_id BIGINT COMMENT '고객사 ID (커스텀 릴리즈인 경우)',
    version VARCHAR(50) NOT NULL UNIQUE COMMENT '버전 번호 (예: 1.1.0)',
    major_version INT NOT NULL COMMENT '메이저 버전',
    minor_version INT NOT NULL COMMENT '마이너 버전',
    patch_version INT NOT NULL COMMENT '패치 버전',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    comment TEXT COMMENT '버전 설명',
    custom_version VARCHAR(100) COMMENT '커스텀 버전명',
    is_install BOOLEAN NOT NULL DEFAULT FALSE COMMENT '설치 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_release_type (release_type),
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
    file_category VARCHAR(50) COMMENT '파일 카테고리 (DATABASE, WEB, INSTALL, ENGINE)',
    sub_category VARCHAR(50) COMMENT '하위 카테고리 (MARIADB, CRATEDB, METADATA 등)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (물리 경로)',
    relative_path VARCHAR(500) COMMENT 'ZIP 파일 내부 상대 경로',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (MD5)',
    execution_order INT NOT NULL DEFAULT 1 COMMENT '실행 순서',
    is_build_artifact BOOLEAN DEFAULT FALSE COMMENT '빌드 산출물 여부',
    description TEXT COMMENT '파일 설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_release_version_id (release_version_id),
    INDEX idx_file_type (file_type),
    INDEX idx_file_category (file_category),
    INDEX idx_sub_category (sub_category),
    INDEX idx_file_path (file_path),
    INDEX idx_execution_order (execution_order),
    INDEX idx_is_build_artifact (is_build_artifact),

    CONSTRAINT fk_release_file_version FOREIGN KEY (release_version_id)
        REFERENCES release_version(release_version_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 파일 테이블';

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
    generated_at DATETIME NOT NULL COMMENT '생성일시',
    generated_by VARCHAR(100) NOT NULL COMMENT '생성자',
    description TEXT COMMENT '설명',
    patched_by VARCHAR(100) COMMENT '패치 담당자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_cp_release_type (release_type),
    INDEX idx_cp_customer_id (customer_id),
    INDEX idx_cp_from_version (from_version),
    INDEX idx_cp_to_version (to_version),
    INDEX idx_cp_generated_at (generated_at),

    CONSTRAINT fk_cumulative_patch_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='누적 패치 생성 이력 테이블';

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
-- Section 8: 코드 타입 기본 데이터
-- =========================================================

INSERT INTO code_type (code_type_id, code_type_name, description) VALUES
('ACCOUNT_ROLE', '계정 권한', '계정 권한 구분'),
('ACCOUNT_STATUS', '계정 상태', '계정 상태 구분'),
('RELEASE_TYPE', '릴리즈 타입', '릴리즈 타입 구분 (표준/커스텀)'),
('DATABASE_TYPE', '데이터베이스 타입', '지원하는 데이터베이스 종류'),
('FILE_TYPE', '파일 타입', '파일 확장자 타입'),
('FILE_CATEGORY', '파일 카테고리', '파일 기능적 대분류'),
('FILE_SUBCATEGORY', '파일 서브 카테고리', '파일 카테고리별 소분류');

-- =========================================================
-- Section 9: 계정 권한 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order) VALUES
('ACCOUNT_ROLE', 'ADMIN', '관리자', '시스템 관리자 권한', 1),
('ACCOUNT_ROLE', 'USER', '일반 사용자', '일반 사용자 권한', 2),
('ACCOUNT_ROLE', 'GUEST', '게스트', '게스트 사용자 권한', 3);

-- =========================================================
-- Section 10: 계정 상태 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order) VALUES
('ACCOUNT_STATUS', 'ACTIVE', '활성', '활성 상태', 1),
('ACCOUNT_STATUS', 'INACTIVE', '비활성', '비활성 상태', 2),
('ACCOUNT_STATUS', 'SUSPENDED', '정지', '정지 상태', 3);

-- =========================================================
-- Section 11: 릴리즈 타입 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RELEASE_TYPE', 'STANDARD', '표준 릴리즈', '모든 고객사 공통 적용 릴리즈', 1, TRUE),
('RELEASE_TYPE', 'CUSTOM', '커스텀 릴리즈', '특정 고객사 전용 릴리즈', 2, TRUE);

-- =========================================================
-- Section 12: 데이터베이스 타입 코드
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('DATABASE_TYPE', 'MARIADB', 'MariaDB', 'MariaDB 데이터베이스', 1, TRUE),
('DATABASE_TYPE', 'CRATEDB', 'CrateDB', 'CrateDB 데이터베이스', 2, TRUE);

-- =========================================================
-- Section 13: 파일 타입 코드 (확장자)
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
-- Section 14: 파일 카테고리 코드 (기능적 대분류)
-- =========================================================

INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_CATEGORY', 'DATABASE', 'DATABASE', '데이터베이스 관련 파일', 1, TRUE),
('FILE_CATEGORY', 'WEB', 'WEB', '웹 애플리케이션 파일', 2, TRUE),
('FILE_CATEGORY', 'INSTALL', 'INSTALL', '설치 관련 파일', 3, TRUE),
('FILE_CATEGORY', 'ENGINE', 'ENGINE', '엔진 관련 파일', 4, TRUE);

-- =========================================================
-- Section 15: 파일 서브 카테고리 코드 (카테고리별 소분류)
-- =========================================================

-- DATABASE 서브 카테고리
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY', 'CRATEDB', 'CRATEDB', 'CrateDB 스크립트', 1, TRUE),
('FILE_SUBCATEGORY', 'MARIADB', 'MARIADB', 'MariaDB 스크립트', 2, TRUE),
('FILE_SUBCATEGORY', 'METADATA', 'METADATA', '메타데이터 파일', 3, TRUE);

-- WEB 서브 카테고리
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY', 'BUILD', 'BUILD', '빌드 산출물', 11, TRUE),
('FILE_SUBCATEGORY', 'WEBOBJECTS', 'WEBOBJECTS', 'WebObjects 파일', 12, TRUE),
('FILE_SUBCATEGORY', 'ETC', 'ETC', '기타 파일', 13, TRUE);

-- INSTALL 서브 카테고리
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY', 'SH', 'SH', '쉘 스크립트', 21, TRUE),
('FILE_SUBCATEGORY', 'IMAGE', 'IMAGE', '이미지 파일', 22, TRUE);

-- ENGINE 서브 카테고리 (BUILD, SH, IMAGE, METADATA, ETC 공유)

-- =========================================================
-- Section 16: 기본 관리자 계정
-- 패스워드: nms12345! (BCrypt 암호화)
-- =========================================================

INSERT INTO account (account_name, email, password, role, status) VALUES
('시스템 관리자','admin@tscientific.co.kr', '$2a$10$l8sMjsX460lFokTzvBuBOefMU0u//xpEzNCV4uhLvr0huqUWpTYPe', 'ADMIN', 'ACTIVE'),
('사용자','m_user@tscientific.co.kr', '$2a$10$l8sMjsX460lFokTzvBuBOefMU0u//xpEzNCV4uhLvr0huqUWpTYPe', 'USER', 'ACTIVE');

-- =========================================================
-- Section 17: 실제 릴리즈 데이터 추가
-- =========================================================

INSERT INTO release_version (
    release_type, customer_id, version,
    major_version, minor_version, patch_version,
    created_by, comment, is_install, created_at
) VALUES
('STANDARD', NULL, '1.0.0', 1, 0, 0, 'TS', '최초 설치본', TRUE, '2025-01-01 00:00:00'),
('STANDARD', NULL, '1.1.0', 1, 1, 0, 'jhlee@tscientific', '데이터코드, 이벤트코드, 메뉴코드 추가 / SMS 기능 추가 / VERSION_HISTORY 테이블 추가 / V_INFO_MCH 관련 뷰 변경', FALSE, '2025-10-31 00:00:00'),
('STANDARD', NULL, '1.1.1', 1, 1, 1, 'jhlee@tscientific', 'SMS - 운영관리 - 파일 기능 관련 테이블 추가', FALSE, '2025-11-05 00:00:00'),
('STANDARD', NULL, '1.1.2', 1, 1, 2, 'jhlee@tscientific', 'SMS - 로그관리 - 로그 모니터 정책 상세 테이블 추가', FALSE, '2025-11-25 00:00:00');

-- =========================================================
-- Section 18: 릴리즈 파일 데이터
-- =========================================================

INSERT INTO release_file (
    release_version_id, file_type, file_category, sub_category, file_name, file_path, relative_path,
    file_size, checksum, execution_order, is_build_artifact, description
) VALUES
-- 1.0.0 - Install Documents
(1, 'PDF', 'INSTALL', NULL, 'Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',
    'versions/standard/1.0.x/1.0.0/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',
    '/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',
    2727778, '4e641f7d25bbaa0061f553b92ef3d9e9', 1, FALSE, '설치 가이드 문서'),
(1, 'MD', 'INSTALL', NULL, '설치본정보.md',
    'versions/standard/1.0.x/1.0.0/install/설치본정보.md',
    '/install/설치본정보.md',
    778, '8e5adf2b877090de4f3ec5739f71216c', 2, FALSE, '설치본 정보'),
-- 1.1.0 - MariaDB
(2, 'SQL', 'DATABASE', 'MARIADB', '1.patch_mariadb_ddl.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/1.patch_mariadb_ddl.sql',
    '/mariadb/1.patch_mariadb_ddl.sql',
    34879, 'f8b9f64345555c9a4a9c9101aaa8b701', 1, FALSE, 'DDL 변경'),
(2, 'SQL', 'DATABASE', 'MARIADB', '2.patch_mariadb_view.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/2.patch_mariadb_view.sql',
    '/mariadb/2.patch_mariadb_view.sql',
    10742, '6735c7267bedc684f155ce05eaa5b7df', 2, FALSE, 'View 변경'),
(2, 'SQL', 'DATABASE', 'MARIADB', '3.patch_mariadb_데이터코드.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/3.patch_mariadb_데이터코드.sql',
    '/mariadb/3.patch_mariadb_데이터코드.sql',
    134540, 'faec479bf1582dfb20199fdd468676f7', 3, FALSE, '데이터 코드 추가'),
(2, 'SQL', 'DATABASE', 'MARIADB', '4.patch_mariadb_이벤트코드.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/4.patch_mariadb_이벤트코드.sql',
    '/mariadb/4.patch_mariadb_이벤트코드.sql',
    36847, 'e2e818dfa626c93894b5774badee0219', 4, FALSE, '이벤트 코드 추가'),
(2, 'SQL', 'DATABASE', 'MARIADB', '5.patch_mariadb_메뉴코드.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/5.patch_mariadb_메뉴코드.sql',
    '/mariadb/5.patch_mariadb_메뉴코드.sql',
    25144, '3eb290c91cf66dacbc02a746bec2bef0', 5, FALSE, '메뉴 코드 추가'),
(2, 'SQL', 'DATABASE', 'MARIADB', '6.patch_mariadb_procedure.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/6.patch_mariadb_procedure.sql',
    '/mariadb/6.patch_mariadb_procedure.sql',
    22183, '25942f2c2201629efcc333278f8eac38', 6, FALSE, 'Procedure 변경'),
(2, 'SQL', 'DATABASE', 'MARIADB', '7.patch_mariadb_dml.sql',
    'versions/standard/1.1.x/1.1.0/mariadb/7.patch_mariadb_dml.sql',
    '/mariadb/7.patch_mariadb_dml.sql',
    37330, '3fa1ec88b5a638fb6d67a41119d61854', 7, FALSE, 'DML 변경'),
-- 1.1.0 - CrateDB
(2, 'SQL', 'DATABASE', 'CRATEDB', '1.patch_cratedb_ddl.sql',
    'versions/standard/1.1.x/1.1.0/cratedb/1.patch_cratedb_ddl.sql',
    '/cratedb/1.patch_cratedb_ddl.sql',
    19363, '1b68614d70c52cade269e5accca724d5', 1, FALSE, 'CrateDB DDL 변경'),
-- 1.1.1 - MariaDB
(3, 'SQL', 'DATABASE', 'MARIADB', '1.patch_mariadb_ddl.sql',
    'versions/standard/1.1.x/1.1.1/mariadb/1.patch_mariadb_ddl.sql',
    '/mariadb/1.patch_mariadb_ddl.sql',
    4867, '848ecec66ce257e0fcec4088294c816d', 1, FALSE, '파일 기능 관련 DDL 추가'),
(3, 'SQL', 'DATABASE', 'MARIADB', '2.patch_mariadb_dml.sql',
    'versions/standard/1.1.x/1.1.1/mariadb/2.patch_mariadb_dml.sql',
    '/mariadb/2.patch_mariadb_dml.sql',
    660, '63fe833edd62599db2ce8c758eae0240', 2, FALSE, '파일 기능 관련 DML 추가'),
-- 1.1.2 - MariaDB
(4, 'SQL', 'DATABASE', 'MARIADB', '1.patch_mariadb_ddl.sql',
    'versions/standard/1.1.x/1.1.2/mariadb/1.patch_mariadb_ddl.sql',
    '/mariadb/1.patch_mariadb_ddl.sql',
    1765, '48bb04f6b3f2f4560ab42c0c37fcacbc', 1, FALSE, 'SMS 로그 모니터링 정책 상세 테이블 추가');

-- =========================================================
-- Section 19: 계층 구조 데이터 추가
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
