-- ============================================================================
-- Release Manager: release_version 테이블 생성
-- 설명: 릴리즈 버전 정보 관리
-- ============================================================================

CREATE TABLE release_version (
    release_version_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 버전 ID',
    release_type_id BIGINT NOT NULL COMMENT '릴리즈 타입 ID',
    customer_id BIGINT NULL COMMENT '고객사 ID (custom인 경우 필수)',
    version VARCHAR(50) NOT NULL COMMENT '버전 (1.1.0)',
    major_version INT NOT NULL COMMENT '메이저 버전 (1)',
    minor_version INT NOT NULL COMMENT '마이너 버전 (1)',
    patch_version INT NOT NULL COMMENT '패치 버전 (0)',
    major_minor VARCHAR(10) NOT NULL COMMENT '메이저.마이너 (1.1.x)',
    created_at TIMESTAMP NOT NULL COMMENT '버전 생성일시',
    created_by VARCHAR(100) NOT NULL COMMENT '생성자',
    comment TEXT COMMENT '버전 코멘트',
    custom_version VARCHAR(50) NULL COMMENT '커스텀 버전 (custom 타입인 경우)',
    is_install BOOLEAN DEFAULT FALSE COMMENT '설치본 여부',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    FOREIGN KEY (release_type_id) REFERENCES release_type(release_type_id) ON DELETE RESTRICT,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT,

    UNIQUE KEY uk_release_version (release_type_id, customer_id, version),
    INDEX idx_version (major_version, minor_version, patch_version),
    INDEX idx_major_minor (major_minor),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 버전 정보';
