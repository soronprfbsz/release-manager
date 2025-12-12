-- =========================================================
-- V1: Release Manager 초기 테이블 생성
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

CREATE TABLE IF NOT EXISTS account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '계정 ID',
    account_name VARCHAR(50) NOT NULL COMMENT '계정 이름',
    email VARCHAR(50) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(100) NOT NULL COMMENT '비밀번호',
    role VARCHAR(100) NOT NULL COMMENT '계정 권한',
    status VARCHAR(100) NOT NULL COMMENT '계정 상태 (ACTIVE, INACTIVE, SUSPENDED)',
    last_login_at DATETIME COMMENT '마지막 로그인 일시',
    login_attempt_count INT NOT NULL DEFAULT 0 COMMENT '로그인 시도 횟수',
    locked_until DATETIME COMMENT '계정 잠금 해제 시간 (10분 임시 잠금)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계정 테이블';

CREATE TABLE IF NOT EXISTS project (
    project_id VARCHAR(50) PRIMARY KEY COMMENT '프로젝트 ID (예: infraeye1, infraeye2)',
    project_name VARCHAR(100) NOT NULL COMMENT '프로젝트 명',
    description TEXT COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '생성자',

    INDEX idx_project_name (project_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='프로젝트 테이블';

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
    release_category VARCHAR(20) NOT NULL DEFAULT 'PATCH' COMMENT '릴리즈 카테고리 (INSTALL/PATCH)',
    customer_id BIGINT COMMENT '고객사 ID (커스텀 릴리즈인 경우)',
    version VARCHAR(50) NOT NULL COMMENT '버전 번호 (예: 1.1.0)',
    major_version INT NOT NULL COMMENT '메이저 버전',
    minor_version INT NOT NULL COMMENT '마이너 버전',
    patch_version INT NOT NULL COMMENT '패치 버전',
    custom_version VARCHAR(100) COMMENT '커스텀 버전',
    comment TEXT COMMENT '버전 설명',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    INDEX idx_project_id (project_id),
    INDEX idx_release_type (release_type),
    INDEX idx_release_category (release_category),
    INDEX idx_customer_id (customer_id),
    INDEX idx_version (version),
    INDEX idx_major_minor (major_version, minor_version),
    INDEX idx_created_at (created_at),

    UNIQUE KEY uk_project_version (project_id, version),

    CONSTRAINT fk_release_version_project FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE RESTRICT,
    CONSTRAINT fk_release_version_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 버전 테이블';

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
    UNIQUE INDEX uk_bf_file_path (file_path) COMMENT '파일 경로 중복 방지',
    INDEX idx_bf_created_at (created_at)
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
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_bfl_backup_file_id (backup_file_id),
    INDEX idx_bfl_log_type (log_type),
    INDEX idx_bfl_log_file_name (log_file_name),
    INDEX idx_bfl_created_at (created_at),

    CONSTRAINT fk_backup_file_log_backup_file FOREIGN KEY (backup_file_id)
        REFERENCES backup_file(backup_file_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='백업 파일 로그 테이블';

CREATE TABLE IF NOT EXISTS department (
    department_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '부서 ID',
    department_name VARCHAR(100) NOT NULL UNIQUE COMMENT '부서명',
    description VARCHAR(500) COMMENT '설명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_department_name (department_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='부서 테이블';

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

CREATE TABLE IF NOT EXISTS cumulative_patch (
    patch_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '패치 ID',
    project_id VARCHAR(50) NOT NULL COMMENT '프로젝트 ID',
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

    INDEX idx_cp_project_id (project_id),
    INDEX idx_cp_release_type (release_type),
    INDEX idx_cp_customer_id (customer_id),
    INDEX idx_cp_from_version (from_version),
    INDEX idx_cp_to_version (to_version),
    INDEX idx_cp_engineer_id (engineer_id),
    INDEX idx_cp_created_at (created_at),

    CONSTRAINT fk_cumulative_patch_project FOREIGN KEY (project_id)
        REFERENCES project(project_id) ON DELETE RESTRICT,
    CONSTRAINT fk_cumulative_patch_customer FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_cumulative_patch_engineer FOREIGN KEY (engineer_id)
        REFERENCES engineer(engineer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='누적 패치 테이블';

CREATE TABLE menu (
    menu_id VARCHAR(50) PRIMARY KEY COMMENT '메뉴 ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '메뉴명',
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
