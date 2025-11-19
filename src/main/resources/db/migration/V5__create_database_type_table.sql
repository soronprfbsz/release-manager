-- ============================================================================
-- Release Manager: database_type 테이블 생성
-- 설명: 데이터베이스 타입 (mariadb/cratedb) 관리
-- ============================================================================

CREATE TABLE database_type (
    database_type_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'DB 타입 ID',
    type_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'DB 타입명 (mariadb/cratedb)',
    description VARCHAR(255) COMMENT '설명',
    is_active BOOLEAN DEFAULT TRUE COMMENT '활성 여부',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',

    INDEX idx_type_name (type_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='데이터베이스 타입';

-- 초기 데이터
INSERT INTO database_type (type_name, description, is_active) VALUES
('mariadb', 'MariaDB 데이터베이스', TRUE),
('cratedb', 'CrateDB 데이터베이스', TRUE);
