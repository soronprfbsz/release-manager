-- ============================================================================
-- Release Manager: customer 테이블 생성
-- 설명: 고객사 정보 관리 (custom 릴리즈용)
-- ============================================================================

CREATE TABLE customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '고객사 ID',
    customer_code VARCHAR(50) NOT NULL UNIQUE COMMENT '고객사 코드 (company_a)',
    customer_name VARCHAR(100) NOT NULL COMMENT '고객사명',
    description TEXT COMMENT '설명',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',

    INDEX idx_customer_code (customer_code),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='고객사 정보';

-- 초기 샘플 데이터 (기존 파일 시스템에서 확인된 고객사)
INSERT INTO customer (customer_code, customer_name, description, is_active) VALUES
('company_a', '고객사 A', '샘플 고객사', TRUE);
