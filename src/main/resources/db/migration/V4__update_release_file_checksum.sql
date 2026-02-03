-- =====================================================
-- V4: 릴리즈 파일 체크섬 및 파일 사이즈 업데이트
-- 대상 파일:
--   - 1.patch_mariadb_ddl.sql
--   - 4.patch_mariadb_메뉴코드.sql
--   - 8.patch_mariadb_dml.sql
-- =====================================================

-- 1.patch_mariadb_ddl.sql 업데이트
-- 기존: file_size=33881, checksum='761c3a421dae8608c67c0f16c565a8e977dbb1b7bbdcb995cbd36511e6466a44'
-- 변경: file_size=33880, checksum='d7f3336ad8cdacb1244f8541a7ca808d5a1dc74629655482147c3a25049cebdd'
UPDATE release_file
SET file_size = 33880,
    checksum = 'd7f3336ad8cdacb1244f8541a7ca808d5a1dc74629655482147c3a25049cebdd',
    updated_at = CURRENT_TIMESTAMP
WHERE file_name = '1.patch_mariadb_ddl.sql'
  AND file_path = 'versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/1.patch_mariadb_ddl.sql';

-- 4.patch_mariadb_메뉴코드.sql 업데이트
-- 기존: file_size=24808, checksum='cbd7a3477ee4ca78e89ab58cce49513714b2186b14452dad09141409cb4e0b36'
-- 변경: file_size=24215, checksum='b4a05f8ecbd98b33e69ec5b6fb99ed2b5034473c67d47a8abf430fcc40a290bb'
UPDATE release_file
SET file_size = 24215,
    checksum = 'b4a05f8ecbd98b33e69ec5b6fb99ed2b5034473c67d47a8abf430fcc40a290bb',
    updated_at = CURRENT_TIMESTAMP
WHERE file_name = '4.patch_mariadb_메뉴코드.sql'
  AND file_path = 'versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/4.patch_mariadb_메뉴코드.sql';

-- 8.patch_mariadb_dml.sql 업데이트
-- 기존: file_size=40466, checksum='e48609ba8654cb4481e875cd4018a32e9bfaa20ba72bd76e2421f96df4a6563c'
-- 변경: file_size=42451, checksum='dfad3c8ebc9f1dc8dc2a3f09cc0918eb7eeba022653d7ae2aa78691afeecca66'
UPDATE release_file
SET file_size = 42451,
    checksum = 'dfad3c8ebc9f1dc8dc2a3f09cc0918eb7eeba022653d7ae2aa78691afeecca66',
    updated_at = CURRENT_TIMESTAMP
WHERE file_name = '8.patch_mariadb_dml.sql'
  AND file_path = 'versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/8.patch_mariadb_dml.sql';

-- =====================================================
-- Rollback (수동 실행 시 사용)
-- =====================================================
-- UPDATE release_file
-- SET file_size = 33881,
--     checksum = '761c3a421dae8608c67c0f16c565a8e977dbb1b7bbdcb995cbd36511e6466a44',
--     updated_at = CURRENT_TIMESTAMP
-- WHERE file_name = '1.patch_mariadb_ddl.sql'
--   AND file_path = 'versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/1.patch_mariadb_ddl.sql';
--
-- UPDATE release_file
-- SET file_size = 24808,
--     checksum = 'cbd7a3477ee4ca78e89ab58cce49513714b2186b14452dad09141409cb4e0b36',
--     updated_at = CURRENT_TIMESTAMP
-- WHERE file_name = '4.patch_mariadb_메뉴코드.sql'
--   AND file_path = 'versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/4.patch_mariadb_메뉴코드.sql';
--
-- UPDATE release_file
-- SET file_size = 40466,
--     checksum = 'e48609ba8654cb4481e875cd4018a32e9bfaa20ba72bd76e2421f96df4a6563c',
--     updated_at = CURRENT_TIMESTAMP
-- WHERE file_name = '8.patch_mariadb_dml.sql'
--   AND file_path = 'versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/8.patch_mariadb_dml.sql';
