-- ============================================================================
-- Release Manager: release_type 테이블 생성
-- 설명: 릴리즈 타입 (standard/custom) 관리
-- ============================================================================

CREATE TABLE release_type (
    release_type_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '릴리즈 타입 ID',
    type_name VARCHAR(50) NOT NULL UNIQUE COMMENT '타입명 (standard/custom)',
    description VARCHAR(255) COMMENT '설명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_type_name (type_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='릴리즈 타입';

-- 초기 데이터
INSERT INTO release_type (type_name, description) VALUES
('standard', '표준 릴리즈'),
('custom', '고객사 커스텀 릴리즈');
