-- Migration 검증 쿼리

-- 1. Flyway 마이그레이션 이력 확인
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 2. 생성된 테이블 목록 확인
SHOW TABLES;

-- 3. release_type 테이블 데이터 확인
SELECT * FROM release_type;

-- 4. customer 테이블 데이터 확인
SELECT * FROM customer;

-- 5. database_type 테이블 데이터 확인
SELECT * FROM database_type;

-- 6. 테이블 구조 확인
DESCRIBE release_version;
DESCRIBE patch_file;
DESCRIBE cumulative_patch;
