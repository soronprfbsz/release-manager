-- =========================================================
-- V1: Release Manager 초기 테이블 생성 (통합 DDL)
-- =========================================================
-- 최종 스키마 반영:
-- - service 테이블: sort_order 컬럼 추가
-- - service_component 테이블: database_name 제거, host/port NOT NULL
-- =========================================================

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
-- 부서 테이블 (account 테이블보다 먼저 생성 - FK 참조)
-- =========================================================

CREATE TABLE IF NOT EXISTS department (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '부서 ID',
    department_name VARCHAR(100) NOT NULL UNIQUE COMMENT '부서명',
    department_type VARCHAR(50) COMMENT '부서 유형 (code_type_id=DEPARTMENT_TYPE 참조: DEVELOPMENT, ENGINEER)',
    description VARCHAR(500) COMMENT '설명',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_department_name (department_name),
    INDEX idx_department_type (department_type),
    INDEX idx_department_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서 테이블';

-- =========================================================
-- 부서 계층 구조 테이블 (Closure Table 패턴)
-- =========================================================
-- ancestor_id: 조상 부서 ID
-- descendant_id: 후손 부서 ID
-- depth: 0=자기자신, 1=직계자식, 2=손자, ...
-- =========================================================

CREATE TABLE IF NOT EXISTS department_hierarchy (
    ancestor_id BIGINT NOT NULL COMMENT '조상 부서 ID',
    descendant_id BIGINT NOT NULL COMMENT '후손 부서 ID',
    depth INT NOT NULL DEFAULT 0 COMMENT '계층 깊이 (0=자기자신, 1=직계자식, 2=손자...)',

    PRIMARY KEY (ancestor_id, descendant_id),

    INDEX idx_dh_ancestor (ancestor_id),
    INDEX idx_dh_descendant (descendant_id),
    INDEX idx_dh_depth (depth),

    CONSTRAINT fk_dh_ancestor FOREIGN KEY (ancestor_id)
        REFERENCES department(department_id) ON DELETE CASCADE,
    CONSTRAINT fk_dh_descendant FOREIGN KEY (descendant_id)
        REFERENCES department(department_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서 계층 구조 테이블 (Closure Table)';

CREATE TABLE IF NOT EXISTS account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '계정 ID',
    account_name VARCHAR(50) NOT NULL COMMENT '계정 이름',
    email VARCHAR(50) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(100) NOT NULL COMMENT '비밀번호',
    avatar_style VARCHAR(50) COMMENT '아바타 스타일 (DiceBear 스타일명)',
    avatar_seed VARCHAR(100) COMMENT '아바타 시드 (랜덤 문자열)',
    phone VARCHAR(20) COMMENT '연락처',
    position VARCHAR(100) COMMENT '직급 (code_type_id=POSITION 참조)',
    department_id BIGINT COMMENT '소속 부서 ID',
    role VARCHAR(100) NOT NULL COMMENT '계정 권한',
    status VARCHAR(100) NOT NULL COMMENT '계정 상태 (ACTIVE, INACTIVE, SUSPENDED)',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',
    login_attempt_count INT NOT NULL DEFAULT 0 COMMENT '로그인 시도 횟수',
    locked_until DATETIME COMMENT '계정 잠금 해제 시간 (10분 임시 잠금)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status),
    INDEX idx_position (position),
    INDEX idx_account_department (department_id),

    CONSTRAINT fk_account_department FOREIGN KEY (department_id)
        REFERENCES department(department_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계정 테이블';

CREATE TABLE IF NOT EXISTS project (
    project_id VARCHAR(50) PRIMARY KEY COMMENT '프로젝트 ID (예: infraeye1, infraeye2)',
    project_name VARCHAR(100) NOT NULL COMMENT '프로젝트 명',
    description TEXT COMMENT '설명',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',

    INDEX idx_project_name (project_name),
    INDEX idx_project_is_enabled (is_enabled),
    INDEX idx_project_created_by (created_by),

    CONSTRAINT fk_project_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='프로젝트 테이블';

CREATE TABLE IF NOT EXISTS customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고객사 ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '고객사 코드',
    customer_name VARCHAR(100) NOT NULL COMMENT '고객사 명',
    description VARCHAR(255) COMMENT '설명',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    updated_by BIGINT COMMENT '수정자 계정 ID (account.account_id FK)',
    updated_by_email VARCHAR(100) COMMENT '수정자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_customer_code (customer_code),
    INDEX idx_customer_name (customer_name),
    INDEX idx_is_active (is_active),
    INDEX idx_customer_created_by (created_by),
    INDEX idx_customer_updated_by (updated_by),

    CONSTRAINT fk_customer_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_customer_updated_by FOREIGN KEY (updated_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='고객사 테이블';

CREATE TABLE IF NOT EXISTS customer_project (
    customer_id BIGINT NOT NULL COMMENT '고객사 ID',
    project_id VARCHAR(50) NOT NULL COMMENT '프로젝트 ID',
    last_patched_version VARCHAR(50) COMMENT '마지막 패치 버전 (to_version)',
    last_patched_at DATETIME COMMENT '마지막 패치 일시',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    PRIMARY KEY (customer_id, project_id),
    INDEX idx_cp_project_id (project_id),
    INDEX idx_cp_last_patched_at (last_patched_at),

    CONSTRAINT fk_customer_project_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_project_project FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='고객사-프로젝트 매핑 테이블';

CREATE TABLE IF NOT EXISTS release_version (
    release_version_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 버전 ID',
    project_id VARCHAR(50) NOT NULL COMMENT '프로젝트 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 릴리즈인 경우)',
    version VARCHAR(50) NOT NULL COMMENT '버전 번호 (예: 1.1.0)',
    major_version INT NOT NULL COMMENT '메이저 버전',
    minor_version INT NOT NULL COMMENT '마이너 버전',
    patch_version INT NOT NULL COMMENT '패치 버전',
    hotfix_version INT NOT NULL DEFAULT 0 COMMENT '핫픽스 버전 (0이면 일반 버전, 1 이상이면 핫픽스)',
    hotfix_base_version_id BIGINT COMMENT '핫픽스 원본 버전 ID (핫픽스인 경우)',
    custom_major_version INT COMMENT '커스텀 메이저 버전',
    custom_minor_version INT COMMENT '커스텀 마이너 버전',
    custom_patch_version INT COMMENT '커스텀 패치 버전',
    custom_base_version_id BIGINT COMMENT '기준 표준 버전 ID (커스텀 릴리즈인 경우)',
    comment TEXT COMMENT '버전 설명',
    is_approved BOOLEAN NOT NULL DEFAULT FALSE COMMENT '승인 여부',
    approved_by BIGINT COMMENT '승인자 계정 ID (account.account_id FK)',
    approved_by_email VARCHAR(100) COMMENT '승인자 이메일 (계정 삭제 시에도 유지)',
    approved_at DATETIME COMMENT '승인일시',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    INDEX idx_project_id (project_id),
    INDEX idx_release_type (release_type),
    INDEX idx_customer_id (customer_id),
    INDEX idx_hotfix_version (hotfix_version),
    INDEX idx_hotfix_base_version_id (hotfix_base_version_id),
    INDEX idx_custom_base_version_id (custom_base_version_id),
    INDEX idx_version (version),
    INDEX idx_major_minor (major_version, minor_version),
    INDEX idx_is_approved (is_approved),
    INDEX idx_created_at (created_at),
    INDEX idx_created_by (created_by),
    INDEX idx_approved_by (approved_by),

    UNIQUE KEY uk_project_type_customer_version (project_id, release_type, customer_id, version, hotfix_version),
    UNIQUE KEY uk_custom_version (customer_id, custom_major_version, custom_minor_version, custom_patch_version),

    CONSTRAINT fk_release_version_project FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE RESTRICT,
    CONSTRAINT fk_release_version_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_release_version_hotfix_base FOREIGN KEY (hotfix_base_version_id)
        REFERENCES release_version(release_version_id) ON DELETE SET NULL,
    CONSTRAINT fk_release_version_custom_base FOREIGN KEY (custom_base_version_id)
        REFERENCES release_version(release_version_id) ON DELETE SET NULL,
    CONSTRAINT fk_release_version_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_release_version_approved_by FOREIGN KEY (approved_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 버전 테이블';

CREATE TABLE IF NOT EXISTS release_file (
    release_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 파일 ID',
    release_version_id BIGINT NOT NULL COMMENT '릴리즈 버전 ID',
    file_type VARCHAR(50) NOT NULL COMMENT '파일 타입 (SQL, PDF, MD, EXE, SH, TXT)',
    file_category VARCHAR(50) COMMENT '파일 카테고리 (DATABASE, WEB, ENGINE, ETC)',
    sub_category VARCHAR(50) COMMENT '하위 카테고리 (MARIADB, CRATEDB, METADATA 등)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (물리 경로)',
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

CREATE TABLE IF NOT EXISTS resource_link (
    resource_link_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '리소스 링크 ID',
    link_category VARCHAR(50) NOT NULL COMMENT '링크 카테고리',
    sub_category VARCHAR(50) COMMENT '하위 카테고리',
    link_name VARCHAR(255) NOT NULL COMMENT '링크 이름',
    link_url VARCHAR(1000) NOT NULL COMMENT '링크 주소',
    description TEXT COMMENT '링크 설명',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서 (link_category 내에서 정렬)',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_rl_link_category (link_category),
    INDEX idx_rl_sub_category (sub_category),
    INDEX idx_rl_link_name (link_name),
    INDEX idx_rl_sort_order (sort_order),
    INDEX idx_rl_created_at (created_at),
    INDEX idx_rl_created_by (created_by),

    CONSTRAINT fk_resource_link_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='리소스 링크 테이블';

-- =========================================================
-- Publishing Tables
-- =========================================================
-- 퍼블리싱(HTML, CSS, JS 등 웹 화면단 리소스) 관리
-- ZIP 파일로 업로드하여 폴더 구조 유지
-- =========================================================

CREATE TABLE IF NOT EXISTS publishing (
    publishing_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '퍼블리싱 ID',
    publishing_name VARCHAR(255) NOT NULL COMMENT '퍼블리싱 명',
    description TEXT COMMENT '퍼블리싱 설명',
    publishing_category VARCHAR(50) NOT NULL COMMENT '카테고리 (code_type: PUBLISHING_CATEGORY)',
    sub_category VARCHAR(50) COMMENT '서브 카테고리 (code_type: PUBLISHING_SUBCATEGORY_XXX)',
    customer_id BIGINT COMMENT '고객사 ID (커스터마이징인 경우)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    updated_by BIGINT COMMENT '수정자 계정 ID (account.account_id FK)',
    updated_by_email VARCHAR(100) COMMENT '수정자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_pub_category (publishing_category),
    INDEX idx_pub_sub_category (sub_category),
    INDEX idx_pub_customer_id (customer_id),
    INDEX idx_pub_sort_order (sort_order),
    INDEX idx_pub_created_at (created_at),
    INDEX idx_pub_created_by (created_by),
    INDEX idx_pub_updated_by (updated_by),

    CONSTRAINT fk_publishing_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_publishing_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_publishing_updated_by FOREIGN KEY (updated_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='퍼블리싱 테이블';

CREATE TABLE IF NOT EXISTS publishing_file (
    publishing_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '퍼블리싱 파일 ID',
    publishing_id BIGINT NOT NULL COMMENT '퍼블리싱 ID',
    file_type VARCHAR(20) NOT NULL COMMENT '파일 타입 (확장자 대문자: HTML, CSS, JS, PNG 등)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (publishing/ 하위 상대경로)',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (SHA-256)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서 (publishing 내에서)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_pf_publishing_id (publishing_id),
    INDEX idx_pf_file_type (file_type),
    INDEX idx_pf_file_name (file_name),
    INDEX idx_pf_file_path (file_path),
    INDEX idx_pf_sort_order (sort_order),

    CONSTRAINT fk_publishing_file_publishing FOREIGN KEY (publishing_id)
        REFERENCES publishing(publishing_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='퍼블리싱 파일 테이블';

CREATE TABLE IF NOT EXISTS backup_file (
    backup_file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '백업 파일 ID',
    file_category VARCHAR(20) NOT NULL COMMENT '파일 카테고리 (MARIADB, CRATEDB)',
    file_type VARCHAR(20) NOT NULL COMMENT '파일 타입 (확장자 대문자, 예: SQL)',
    file_name VARCHAR(255) NOT NULL COMMENT '파일명',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (job/backup_files/ 하위 상대경로)',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (SHA-256)',
    description TEXT COMMENT '파일 설명',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bf_file_category (file_category),
    INDEX idx_bf_file_type (file_type),
    INDEX idx_bf_file_name (file_name),
    UNIQUE INDEX uk_bf_file_path (file_path) COMMENT '파일 경로 중복 방지',
    INDEX idx_bf_created_at (created_at),
    INDEX idx_bf_created_by (created_by),

    CONSTRAINT fk_backup_file_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='백업 파일 테이블';

CREATE TABLE IF NOT EXISTS backup_file_log (
    backup_file_log_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '백업 파일 로그 ID',
    backup_file_id BIGINT NOT NULL COMMENT '백업 파일 ID',
    log_type VARCHAR(20) NOT NULL COMMENT '로그 타입 (BACKUP, RESTORE)',
    log_file_name VARCHAR(255) NOT NULL COMMENT '로그 파일명',
    log_file_path VARCHAR(500) NOT NULL COMMENT '로그 파일 경로 (job/logs/ 하위 상대경로)',
    file_size BIGINT COMMENT '파일 크기 (bytes)',
    checksum VARCHAR(64) COMMENT '파일 체크섬 (SHA-256)',
    description TEXT COMMENT '로그 설명',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bfl_backup_file_id (backup_file_id),
    INDEX idx_bfl_log_type (log_type),
    INDEX idx_bfl_log_file_name (log_file_name),
    INDEX idx_bfl_created_at (created_at),
    INDEX idx_bfl_created_by (created_by),

    CONSTRAINT fk_backup_file_log_backup_file FOREIGN KEY (backup_file_id)
        REFERENCES backup_file(backup_file_id) ON DELETE CASCADE,
    CONSTRAINT fk_backup_file_log_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='백업 파일 로그 테이블';

CREATE TABLE IF NOT EXISTS patch_file (
    patch_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '패치 ID',
    project_id VARCHAR(50) NOT NULL COMMENT '프로젝트 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 패치인 경우)',
    from_version VARCHAR(50) NOT NULL COMMENT '시작 버전',
    to_version VARCHAR(50) NOT NULL COMMENT '종료 버전',
    patch_name VARCHAR(100) NOT NULL COMMENT '패치 파일명',
    output_path VARCHAR(500) NOT NULL COMMENT '생성된 패치 파일 경로',
    description TEXT COMMENT '설명',
    assignee_id BIGINT COMMENT '패치 담당자 (account.account_id FK)',
    assignee_email VARCHAR(100) COMMENT '담당자 이메일 (계정 삭제 시에도 유지)',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_pf_project_id (project_id),
    INDEX idx_pf_release_type (release_type),
    INDEX idx_pf_customer_id (customer_id),
    INDEX idx_pf_from_version (from_version),
    INDEX idx_pf_to_version (to_version),
    INDEX idx_pf_assignee_id (assignee_id),
    INDEX idx_pf_created_at (created_at),
    INDEX idx_pf_created_by (created_by),

    CONSTRAINT fk_patch_file_project FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE RESTRICT,
    CONSTRAINT fk_patch_file_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_patch_file_assignee FOREIGN KEY (assignee_id)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_patch_file_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='패치 파일 테이블';

CREATE TABLE IF NOT EXISTS patch_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '이력 ID',
    project_id VARCHAR(50) NOT NULL COMMENT '프로젝트 ID',
    release_type VARCHAR(20) NOT NULL COMMENT '릴리즈 타입 (STANDARD/CUSTOM)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 패치인 경우)',
    from_version VARCHAR(50) NOT NULL COMMENT '시작 버전',
    to_version VARCHAR(50) NOT NULL COMMENT '종료 버전',
    patch_name VARCHAR(100) NOT NULL COMMENT '패치 파일명',
    description TEXT COMMENT '설명',
    assignee_id BIGINT COMMENT '패치 담당자 (account.account_id FK)',
    assignee_email VARCHAR(100) COMMENT '담당자 이메일 (계정 삭제 시에도 유지)',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',

    INDEX idx_ph_project_id (project_id),
    INDEX idx_ph_release_type (release_type),
    INDEX idx_ph_customer_id (customer_id),
    INDEX idx_ph_from_version (from_version),
    INDEX idx_ph_to_version (to_version),
    INDEX idx_ph_assignee_id (assignee_id),
    INDEX idx_ph_created_at (created_at),
    INDEX idx_ph_created_by (created_by),

    CONSTRAINT fk_patch_history_project FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE RESTRICT,
    CONSTRAINT fk_patch_history_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_patch_history_assignee FOREIGN KEY (assignee_id)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_patch_history_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='패치 이력 테이블 (삭제 없이 영구 보존)';

CREATE TABLE menu (
    menu_id VARCHAR(50) PRIMARY KEY COMMENT '메뉴 ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '메뉴명',
    menu_url VARCHAR(200) COMMENT '메뉴 URL',
    icon VARCHAR(50) COMMENT '메뉴 아이콘 (Lucide React 아이콘명)',
    is_icon_visible BOOLEAN DEFAULT TRUE COMMENT '아이콘 표시 여부',
    description VARCHAR(500) COMMENT '메뉴 설명',
    is_description_visible BOOLEAN DEFAULT TRUE COMMENT '설명 표시 여부',
    is_line_break BOOLEAN DEFAULT FALSE COMMENT '줄바꿈 여부 (가로 배치 시 강제 줄바꿈)',
    menu_order INT NOT NULL COMMENT '메뉴 순서',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴';

CREATE TABLE menu_hierarchy (
    ancestor VARCHAR(50) NOT NULL COMMENT '조상 메뉴 ID',
    descendant VARCHAR(50) NOT NULL COMMENT '자손 메뉴 ID',
    depth INT NOT NULL COMMENT '계층 깊이 (0=자기자신, 1=직계자식)',
    PRIMARY KEY (ancestor, descendant),
    FOREIGN KEY (ancestor) REFERENCES menu(menu_id) ON DELETE CASCADE,
    FOREIGN KEY (descendant) REFERENCES menu(menu_id) ON DELETE CASCADE,
    INDEX idx_descendant (descendant),
    INDEX idx_depth (depth)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴 계층 구조';

CREATE TABLE menu_role (
    menu_id VARCHAR(50) NOT NULL COMMENT '메뉴 ID',
    role VARCHAR(50) NOT NULL COMMENT '역할 (ADMIN, USER, GUEST)',
    PRIMARY KEY (menu_id, role),
    FOREIGN KEY (menu_id) REFERENCES menu(menu_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='메뉴별 접근 권한';

-- =========================================================
-- Terminal Table
-- =========================================================
-- 웹 터미널 세션 정보 저장
-- =========================================================

CREATE TABLE IF NOT EXISTS terminal (
    terminal_id VARCHAR(100) PRIMARY KEY COMMENT '터미널 ID (세션 단위, UUID 형식)',
    host VARCHAR(255) NOT NULL COMMENT '호스트 주소',
    port INT NOT NULL COMMENT 'SSH 포트',
    username VARCHAR(100) NOT NULL COMMENT '사용자명',
    status VARCHAR(20) NOT NULL COMMENT '터미널 상태 (CONNECTING, CONNECTED, DISCONNECTED, ERROR)',
    owner_email VARCHAR(100) NOT NULL COMMENT '소유자 이메일',
    last_activity_at DATETIME COMMENT '마지막 활동 시각',
    expires_at DATETIME COMMENT '만료 시각',
    disconnected_at DATETIME COMMENT '종료 시각',
    error_message VARCHAR(2000) COMMENT '오류 메시지',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',
    INDEX idx_owner_email (owner_email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='웹 터미널';

-- =========================================================
-- Service Management Tables (최종 스키마)
-- =========================================================
-- 변경사항:
-- - service 테이블: sort_order 컬럼 추가 및 인덱스 추가
-- - service_component 테이블:
--   * database_name 컬럼 제거
--   * host, port 컬럼 NOT NULL 제약 추가
-- =========================================================

-- 서비스 테이블
CREATE TABLE IF NOT EXISTS service (
    service_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '서비스 ID',
    service_name VARCHAR(255) NOT NULL COMMENT '서비스명',
    service_type VARCHAR(50) NOT NULL COMMENT '서비스 분류 (SERVICE_TYPE 코드)',
    description TEXT COMMENT '설명',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서 (SERVICE_TYPE의 sort_order 값)',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_by BIGINT COMMENT '수정자 계정 ID (account.account_id FK)',
    updated_by_email VARCHAR(100) COMMENT '수정자 이메일 (계정 삭제 시에도 유지)',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_service_type (service_type),
    INDEX idx_service_name (service_name),
    INDEX idx_sort_order (sort_order),
    INDEX idx_created_at (created_at),
    INDEX idx_service_created_by (created_by),
    INDEX idx_service_updated_by (updated_by),

    CONSTRAINT fk_service_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_service_updated_by FOREIGN KEY (updated_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스 관리';

-- 서비스 컴포넌트 (접속 정보) 테이블
CREATE TABLE IF NOT EXISTS service_component (
    component_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '접속 정보 ID',
    service_id BIGINT NOT NULL COMMENT '서비스 ID',
    component_type VARCHAR(50) NOT NULL COMMENT '접속 유형 (COMPONENT_TYPE 코드)',
    component_name VARCHAR(255) NOT NULL COMMENT '접속 정보명',
    host VARCHAR(255) NOT NULL COMMENT '호스트 주소',
    port INT NOT NULL COMMENT '포트 번호',
    url VARCHAR(500) COMMENT 'URL (WEB 타입용)',
    ssh_port INT COMMENT 'SSH 포트번호',
    description TEXT COMMENT '설명',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    created_by BIGINT COMMENT '생성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '생성자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_by BIGINT COMMENT '수정자 계정 ID (account.account_id FK)',
    updated_by_email VARCHAR(100) COMMENT '수정자 이메일 (계정 삭제 시에도 유지)',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_service_id (service_id),
    INDEX idx_component_type (component_type),
    INDEX idx_component_name (component_name),
    INDEX idx_sort_order (sort_order),
    INDEX idx_component_created_by (created_by),
    INDEX idx_component_updated_by (updated_by),

    CONSTRAINT fk_component_service FOREIGN KEY (service_id) REFERENCES service(service_id) ON DELETE CASCADE,
    CONSTRAINT fk_component_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_component_updated_by FOREIGN KEY (updated_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='서비스 컴포넌트 (접속 정보)';

-- =========================================================
-- 파일 동기화 무시 목록 테이블
-- =========================================================
-- 파일 동기화 분석 시 제외할 파일 경로를 관리합니다.
-- IGNORE 액션 적용 시 이 테이블에 등록되며, 이후 분석에서 제외됩니다.

CREATE TABLE IF NOT EXISTS file_sync_ignore (
    ignore_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '무시 항목 ID',
    file_path VARCHAR(500) NOT NULL COMMENT '파일 경로 (상대 경로)',
    target_type VARCHAR(50) NOT NULL COMMENT '대상 유형 (RELEASE_FILE, RESOURCE_FILE, BACKUP_FILE)',
    status VARCHAR(50) NOT NULL COMMENT '무시 당시 상태 (UNREGISTERED, FILE_MISSING, SIZE_MISMATCH, CHECKSUM_MISMATCH)',
    ignored_by VARCHAR(100) NOT NULL COMMENT '무시 처리자 (이메일)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    UNIQUE KEY uk_file_sync_ignore_path_target (file_path, target_type),
    INDEX idx_file_sync_ignore_target_type (target_type),
    INDEX idx_file_sync_ignore_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='파일 동기화 무시 목록';

-- =========================================================
-- 고객사별 특이사항/메모 관리를 위한 테이블
-- =========================================================
CREATE TABLE IF NOT EXISTS customer_note (
    note_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '특이사항 ID',
    customer_id BIGINT NOT NULL COMMENT '고객사 ID',
    title VARCHAR(200) NOT NULL COMMENT '제목',
    content TEXT NOT NULL COMMENT '내용',
    created_by BIGINT COMMENT '작성자 계정 ID (account.account_id FK)',
    created_by_email VARCHAR(100) COMMENT '작성자 이메일 (계정 삭제 시에도 유지)',
    updated_by BIGINT COMMENT '수정자 계정 ID (account.account_id FK)',
    updated_by_email VARCHAR(100) COMMENT '수정자 이메일 (계정 삭제 시에도 유지)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_cn_customer_id (customer_id),
    INDEX idx_cn_created_at (created_at),
    INDEX idx_cn_created_by (created_by),
    INDEX idx_cn_updated_by (updated_by),
    CONSTRAINT fk_customer_note_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE CASCADE,
    CONSTRAINT fk_customer_note_created_by FOREIGN KEY (created_by)
        REFERENCES account(account_id) ON DELETE SET NULL,
    CONSTRAINT fk_customer_note_updated_by FOREIGN KEY (updated_by)
        REFERENCES account(account_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='고객사 특이사항 테이블';

