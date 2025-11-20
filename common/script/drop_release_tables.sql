-- =========================================================
-- Release Manager 테이블 강제 삭제 스크립트
-- 작성일: 2025-11-20
-- 설명: release_manager 데이터베이스의 모든 릴리즈 관련 테이블 삭제
-- =========================================================

-- Step 1: 외래키 제약조건이 있는 테이블을 역순으로 삭제
-- (자식 테이블부터 삭제 후 부모 테이블 삭제)

-- 1. release_file 테이블 삭제 (release_version 참조)
DROP TABLE IF EXISTS release_file;

-- 2. cumulative_patch_history 테이블 삭제 (customer 참조)
DROP TABLE IF EXISTS cumulative_patch_history;

-- 3. release_version 테이블 삭제 (customer 참조)
DROP TABLE IF EXISTS release_version;

-- 4. customer 테이블 삭제
DROP TABLE IF EXISTS customer;

-- Step 2: Code 테이블의 RELEASE_TYPE, DATABASE_TYPE 데이터 삭제
DELETE FROM code WHERE code_type_id IN ('RELEASE_TYPE', 'DATABASE_TYPE');
DELETE FROM code_type WHERE code_type_id IN ('RELEASE_TYPE', 'DATABASE_TYPE');

-- Step 3: Flyway 히스토리에서 V2 마이그레이션 기록 삭제 (선택사항)
-- DELETE FROM flyway_schema_history WHERE version = '2';

-- =========================================================
-- 확인 쿼리
-- =========================================================
-- SELECT table_name FROM information_schema.tables
-- WHERE table_schema = 'release_manager'
-- ORDER BY table_name;
