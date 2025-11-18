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
    code_id VARCHAR(100) PRIMARY KEY COMMENT '코드 ID',
    code_type_id VARCHAR(50) NOT NULL COMMENT '코드 타입',
    code_name VARCHAR(100) NOT NULL COMMENT '코드 명',
    description VARCHAR(200) COMMENT '설명',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '정렬 순서',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용 여부',
    INDEX idx_code_type_id (code_type_id),
    INDEX idx_is_enabled (is_enabled),
    CONSTRAINT fk_code_type FOREIGN KEY (code_type_id) REFERENCES code_type(code_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='코드 테이블';

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
    INDEX idx_status (status),
    CONSTRAINT fk_account_role FOREIGN KEY (role) REFERENCES code(code_id),
    CONSTRAINT fk_account_status FOREIGN KEY (status) REFERENCES code(code_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='계정 테이블';

-- 코드 타입 기본 데이터
INSERT INTO code_type (code_type_id, code_type_name, description) VALUES
('ACCOUNT_ROLE', '계정 권한', '계정 권한 구분'),
('ACCOUNT_STATUS', '계정 상태', '계정 상태 구분');

-- 계정 권한 코드
INSERT INTO code (code_id, code_type_id, code_name, description, sort_order) VALUES
('ACCOUNT_ROLE_ADMIN', 'ACCOUNT_ROLE', '관리자', '시스템 관리자 권한', 1),
('ACCOUNT_ROLE_USER', 'ACCOUNT_ROLE', '일반 사용자', '일반 사용자 권한', 2);

-- 계정 상태 코드
INSERT INTO code (code_id, code_type_id, code_name, description, sort_order) VALUES
('ACCOUNT_STATUS_ACTIVE', 'ACCOUNT_STATUS', '활성', '활성 상태', 1),
('ACCOUNT_STATUS_INACTIVE', 'ACCOUNT_STATUS', '비활성', '비활성 상태', 2),
('ACCOUNT_STATUS_SUSPENDED', 'ACCOUNT_STATUS', '정지', '정지 상태', 3);

-- 기본 관리자 계정
INSERT INTO account (account_name, email, password, role, status) VALUES
('시스템 관리자','admin@tscientific.co.kr', 'nms12345!', 'ACCOUNT_ROLE_ADMIN', 'ACCOUNT_STATUS_ACTIVE'),
('사용자','m_user@tscientific.co.kr', 'nms12345!', 'ACCOUNT_ROLE_USER', 'ACCOUNT_STATUS_ACTIVE');
