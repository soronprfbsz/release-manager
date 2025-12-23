-- =========================================================
-- V1_2: Release Manager 초기 데이터 삽입 (통합 DML)
-- =========================================================
-- 테이블별로 INSERT문을 그룹화하여 관리 용이성 향상
-- =========================================================

-- =========================================================
-- code_type 테이블 (모든 코드 타입)
-- =========================================================

INSERT INTO code_type (code_type_id, code_type_name, description, is_enabled) VALUES
('ACCOUNT_ROLE', '계정 권한', '계정 권한 구분', TRUE),
('ACCOUNT_STATUS', '계정 상태', '계정 상태 구분', TRUE),
('RELEASE_TYPE', '릴리즈 타입', '릴리즈 타입 구분 (표준/커스텀)', TRUE),
('RELEASE_CATEGORY', '릴리즈 카테고리', '릴리즈 카테고리 구분 (설치본/패치본)', TRUE),
('DATABASE_TYPE', '데이터베이스 타입', '지원하는 데이터베이스 종류', TRUE),
('FILE_TYPE', '파일 타입', '파일 확장자 타입', TRUE),
('FILE_CATEGORY', '파일 카테고리', '파일 기능적 대분류', TRUE),
('FILE_SUBCATEGORY_DATABASE', '데이터베이스 파일 서브 카테고리', 'DATABASE 카테고리 소분류', TRUE),
('FILE_SUBCATEGORY_ENGINE', '엔진 파일 서브 카테고리', 'ENGINE 카테고리 소분류', TRUE),
('RESOURCE_FILE_CATEGORY', '리소스 파일 카테고리', '리소스 파일 기능적 대분류', TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', '스크립트 서브 카테고리', 'SCRIPT 카테고리 소분류', TRUE),
('RESOURCE_SUBCATEGORY_DOCKER', 'Docker 서브 카테고리', 'DOCKER 카테고리 소분류', TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', '문서 서브 카테고리', 'DOCUMENT 카테고리 소분류', TRUE),
('BACKUP_FILE_CATEGORY', '백업 파일 카테고리', '백업 파일 분류 (MARIADB, CRATEDB)', TRUE),
('POSITION', '직급', '엔지니어 직급 구분', TRUE),
('SERVICE_TYPE', '서비스 분류', '서비스 관리에서 사용하는 서비스 분류', TRUE),
('COMPONENT_TYPE', '컴포넌트 유형', '서비스 컴포넌트(접속 정보) 유형', TRUE),
('LINK_CATEGORY', '링크 카테고리', '리소스 링크 카테고리 분류', TRUE),
('LINK_SUBCATEGORY', '링크 서브 카테고리', '리소스 링크 서브 카테고리', TRUE),
('FILE_SYNC_STATUS', '파일 동기화 상태', '파일시스템과 DB 메타데이터 간의 동기화 상태', TRUE),
('FILE_SYNC_TARGET', '파일 동기화 대상', '동기화 가능한 파일 유형', TRUE),
('FILE_SYNC_ACTION', '파일 동기화 액션', '불일치 항목에 대해 수행할 수 있는 액션', TRUE);

-- =========================================================
-- code 테이블 (모든 코드 데이터)
-- =========================================================

-- ACCOUNT_ROLE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('ACCOUNT_ROLE', 'ADMIN', '관리자', '시스템 관리자 권한', 1, TRUE),
('ACCOUNT_ROLE', 'USER', '일반 사용자', '일반 사용자 권한', 2, TRUE),
('ACCOUNT_ROLE', 'GUEST', '게스트', '게스트 사용자 권한', 3, TRUE);

-- ACCOUNT_STATUS
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('ACCOUNT_STATUS', 'ACTIVE', '활성', '활성 상태', 1, TRUE),
('ACCOUNT_STATUS', 'INACTIVE', '비활성', '비활성 상태', 2, TRUE),
('ACCOUNT_STATUS', 'SUSPENDED', '정지', '정지 상태', 3, TRUE);

-- RELEASE_TYPE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RELEASE_TYPE', 'STANDARD', '표준 릴리즈', '모든 고객사 공통 적용 릴리즈', 1, TRUE),
('RELEASE_TYPE', 'CUSTOM', '커스텀 릴리즈', '특정 고객사 전용 릴리즈', 2, TRUE);

-- RELEASE_CATEGORY
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RELEASE_CATEGORY', 'INSTALL', '설치본', '최초 설치용 릴리즈', 1, TRUE),
('RELEASE_CATEGORY', 'PATCH', '패치본', '업데이트용 패치 릴리즈', 2, TRUE);

-- DATABASE_TYPE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('DATABASE_TYPE', 'MARIADB', 'MariaDB', 'MariaDB 데이터베이스', 1, TRUE),
('DATABASE_TYPE', 'CRATEDB', 'CrateDB', 'CrateDB 데이터베이스', 2, TRUE);

-- FILE_TYPE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_TYPE', 'SQL', 'SQL', 'SQL 스크립트 파일', 1, TRUE),
('FILE_TYPE', 'MD', 'MD', '마크다운 문서 파일', 2, TRUE),
('FILE_TYPE', 'PDF', 'PDF', 'PDF 문서 파일', 3, TRUE),
('FILE_TYPE', 'EXE', 'EXE', '실행 파일', 4, TRUE),
('FILE_TYPE', 'SH', 'SH', '쉘 스크립트 파일', 5, TRUE),
('FILE_TYPE', 'TXT', 'TXT', '텍스트 파일', 6, TRUE),
('FILE_TYPE', 'JAR', 'JAR', 'Java Archive 파일', 7, TRUE),
('FILE_TYPE', 'WAR', 'WAR', 'Web Archive 파일', 8, TRUE),
('FILE_TYPE', 'TAR', 'TAR', 'TAR 압축 파일', 9, TRUE),
('FILE_TYPE', 'GZ', 'GZ', 'GZIP 압축 파일', 10, TRUE),
('FILE_TYPE', 'ZIP', 'ZIP', 'ZIP 압축 파일', 11, TRUE),
('FILE_TYPE', 'JSON', 'JSON', 'JSON 데이터 파일', 12, TRUE),
('FILE_TYPE', 'UNDEFINED', 'UNDEFINED', '정의되지 않은 파일 타입', 99, TRUE);

-- FILE_CATEGORY
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_CATEGORY', 'DATABASE', 'DATABASE', '데이터베이스 관련 파일', 1, TRUE),
('FILE_CATEGORY', 'WEB', 'WEB', '웹 애플리케이션 파일', 2, TRUE),
('FILE_CATEGORY', 'ENGINE', 'ENGINE', '엔진 관련 파일', 3, TRUE),
('FILE_CATEGORY', 'ETC', 'ETC', '기타 파일', 4, TRUE);

-- FILE_SUBCATEGORY_DATABASE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY_DATABASE', 'CRATEDB', 'CRATEDB', 'CrateDB 스크립트', 1, TRUE),
('FILE_SUBCATEGORY_DATABASE', 'MARIADB', 'MARIADB', 'MariaDB 스크립트', 2, TRUE),
('FILE_SUBCATEGORY_DATABASE', 'ETC', 'ETC', '기타 파일', 3, TRUE);

-- FILE_SUBCATEGORY_ENGINE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SUBCATEGORY_ENGINE', 'NC_AI_EVENT', 'NC_AI_EVENT', '', 1, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_AI_LEARN', 'NC_AI_LEARN', '', 2, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_AI_MGR', 'NC_AI_MGR', '', 3, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_AP', 'NC_AP', '무선 AP 구성, 수집, 알재비 이벤트 발생', 4, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_API_AP', 'NC_API_AP', 'api 연동으로 무선AP 수집', 5, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_API_KAL', 'NC_API_KAL', '대한항공 api 연동 엔진', 6, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_ARP', 'NC_ARP', 'ARP 정보 수집 엔진_NC_CUSTOM의 브릿지엔진(ARP_MAC_SCAN)', 7, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CONF', 'NC_CONF', '장비 관리', 8, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CONFIBACK', 'NC_CONFIBACK', '각 장비에서 커맨드 또는 스크립트 주기적으로 실행', 9, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CPPM_CHK', 'NC_CPPM_CHK', '삼성전자 CPPM 체크 엔진', 10, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_CUSTOM', 'NC_CUSTOM', '사이트 별 커스텀 가능한 필요한 정우 사용', 11, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_DB_MIG', 'NC_DB_MIG', 'DB 마이그레이션 엔진', 12, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_DPI_KUMOH', 'NC_DPI_KUMOH', '금오공대 DPI엔동 엔진', 13, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EMS', 'NC_EMS', '', 14, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EVENT_SENDER', 'NC_EVENT_SENDER', '외부 이벤트 연동 엔진 개발', 15, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EVENTPUSHER', 'NC_EVENTPUSHER', '', 16, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_EXEC', 'NC_EXEC', '', 17, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FAULT_CP', 'NC_FAULT_CP', '', 18, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FAULT_EX', 'NC_FAULT_EX', '', 19, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FAULT_MS', 'NC_FAULT_MS', '각 엔진의 이벤트를 발생/복구 처리', 20, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_FMS', 'NC_FMS', '설비 장비를 대상으로 구성정보와 성능정보를 수집', 21, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_HTTP_SVR', 'NC_HTTP_SVR', 'http listen server 엔진. 수신 시 설정에 따른 이벤트 처리', 22, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPSLA', 'NC_IPSLA', '', 23, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPT', 'NC_IPT', '', 24, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPT_CDR', 'NC_IPT_CDR', '', 25, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_IPTMAC', 'NC_IPTMAC', '', 26, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_KNB', 'NC_KNB', '', 27, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_L4', 'NC_L4', 'L4 엔진 로그 및 XML 분석', 28, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_L4LB', 'NC_L4LB', '', 29, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_L7', 'NC_L7', 'L7 수집 엔진', 30, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_MIB_PARSER', 'NC_MIB_PARSER', '', 31, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NAC_KUMOH', 'NC_NAC_KUMOH', '금오공대 아상전우 연동 엔진(NAC)', 32, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NDI', 'NC_NDI', '', 33, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NET_SCAN', 'NC_NET_SCAN', '', 34, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NOTI', 'NC_NOTI', '이벤트 알림', 35, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_NOTI_TCPCLIENT', 'NC_NOTI_TCPCLIENT', 'NC_NOTI TCP 연동을 위한 엔진', 36, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_PACKET', 'NC_PACKET', '', 37, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_PERF', 'NC_PERF', '성능 데이터 수집', 38, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_PERF_LEARN', 'NC_PERF_LEARN', '', 39, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REPEAT_EVENT', 'NC_REPEAT_EVENT', '메리츠증권 반복장애 이벤트 처리 엔진', 40, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REQUEST_URL', 'NC_REQUEST_URL', '', 41, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REQUEST_URL_NOKIA', 'NC_REQUEST_URL_NOKIA', 'nokia kafka 연동 이벤트처리 엔진', 42, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_REST_API', 'NC_REST_API', '', 43, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_RT_TOOL', 'NC_RT_TOOL', '동록된 장치에 ICMP, SNMP를 요청 로그를 수집', 44, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_RTT_CLI', 'NC_RTT_CLI', '', 45, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SAMPLE', 'NC_SAMPLE', '', 46, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SDN', 'NC_SDN', '', 47, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SFLOW_C', 'NC_SFLOW_C', '', 48, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SLB', 'NC_SLB', 'ssh/telnet 접속하여 L4 명령어 기반 수집 엔진', 49, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SMS', 'NC_SMS', '서버 구성정보 및 성능정보를 수집', 50, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_Snmp', 'NC_Snmp', '', 51, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SNMP3_CHK', 'NC_SNMP3_CHK', 'snmpv3 engine_id 체크 엔진', 52, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SVC_CHK', 'NC_SVC_CHK', 'Port 및 URL 체크', 53, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_SYSTRAP', 'NC_SYSTRAP', 'syslog와 snmp trap 로그 수집 및 이벤트 발생', 54, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_TMS', 'NC_TMS', 'TMS engine', 55, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_TRACERT', 'NC_TRACERT', '', 56, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_UI_CP', 'NC_UI_CP', 'UI에서 요청하는 command와 snmp 명령을 수행하여 결과를 리턴', 57, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_UPS', 'NC_UPS', 'UPS 장비의 정보 및 상태 확인', 58, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_UTIL', 'NC_UTIL', '', 59, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_VMM', 'NC_VMM', '', 60, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_VPN', 'NC_VPN', 'ssh/telnet 접속하여 터널정보를 수집', 61, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_WATCHDOG', 'NC_WATCHDOG', 'NMS 엔진 관리 및 스케줄 작업 관리 수행', 62, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'NC_X25', 'NC_X25', '', 63, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'OZ_CCTV', 'OZ_CCTV', '', 64, TRUE),
('FILE_SUBCATEGORY_ENGINE', 'ETC', 'ETC', '기타 파일', 65, TRUE);

-- RESOURCE_FILE_CATEGORY
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_FILE_CATEGORY', 'SCRIPT', '스크립트', '스크립트 파일 (백업, 복원 등)', 1, TRUE),
('RESOURCE_FILE_CATEGORY', 'DOCKER', 'Docker', 'Docker 관련 파일 (컴포즈, Dockerfile 등)', 2, TRUE),
('RESOURCE_FILE_CATEGORY', 'DOCUMENT', '문서', '설치 가이드 및 기타 문서', 3, TRUE),
('RESOURCE_FILE_CATEGORY', 'ETC', '기타', '기타 리소스 파일', 99, TRUE);

-- RESOURCE_SUBCATEGORY_SCRIPT
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_SUBCATEGORY_SCRIPT', 'MARIADB', 'MariaDB', 'MariaDB 관련 스크립트', 1, TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', 'CRATEDB', 'CrateDB', 'CrateDB 관련 스크립트', 2, TRUE),
('RESOURCE_SUBCATEGORY_SCRIPT', 'ETC', '기타', '기타 스크립트', 99, TRUE);

-- RESOURCE_SUBCATEGORY_DOCKER
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_SUBCATEGORY_DOCKER', 'SERVICE', '서비스 실행', 'Docker 서비스 실행 관련 파일', 1, TRUE),
('RESOURCE_SUBCATEGORY_DOCKER', 'DOCKERFILE', 'Dockerfile', 'Dockerfile 및 빌드 관련 파일', 2, TRUE),
('RESOURCE_SUBCATEGORY_DOCKER', 'ETC', '기타', '기타 Docker 파일', 99, TRUE);

-- RESOURCE_SUBCATEGORY_DOCUMENT
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('RESOURCE_SUBCATEGORY_DOCUMENT', 'INFRAEYE1', 'Infraeye 1', 'Infraeye 1 관련 문서', 1, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'INFRAEYE2', 'Infraeye 2', 'Infraeye 2 관련 문서', 2, TRUE),
('RESOURCE_SUBCATEGORY_DOCUMENT', 'ETC', '기타', '기타 문서', 99, TRUE);

-- BACKUP_FILE_CATEGORY
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('BACKUP_FILE_CATEGORY', 'MARIADB', 'MariaDB', 'MariaDB 백업 파일', 1, TRUE),
('BACKUP_FILE_CATEGORY', 'CRATEDB', 'CrateDB', 'CrateDB 백업 파일', 2, TRUE);

-- POSITION
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('POSITION', 'DIRECTOR', '이사', '이사', 1, TRUE),
('POSITION', 'GENERAL_MANAGER', '부장', '부장', 2, TRUE),
('POSITION', 'DEPUTY_MANAGER', '차장', '차장', 3, TRUE),
('POSITION', 'MANAGER', '과장', '과장', 4, TRUE),
('POSITION', 'ASSISTANT_MANAGER', '대리', '대리', 5, TRUE),
('POSITION', 'STAFF', '사원', '사원', 6, TRUE);

-- SERVICE_TYPE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('SERVICE_TYPE', 'infra', '개발/운영 인프라', '개발 및 운영 인프라 관련 서비스', 1, TRUE),
('SERVICE_TYPE', 'infraeye1', 'Infraeye 1', 'Infraeye 1 관련 서비스', 2, TRUE),
('SERVICE_TYPE', 'infraeye2', 'Infraeye 2', 'Infraeye 2 관련 서비스', 3, TRUE),
('SERVICE_TYPE', 'etc', '기타', '기타 서비스', 99, TRUE);

-- COMPONENT_TYPE
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('COMPONENT_TYPE', 'WEB', '웹', '웹 접속 정보', 1, TRUE),
('COMPONENT_TYPE', 'DATABASE', '데이터베이스', '데이터베이스 접속 정보', 2, TRUE),
('COMPONENT_TYPE', 'ENGINE', '엔진', '엔진 접속 정보', 3, TRUE),
('COMPONENT_TYPE', 'ETC', '기타', '기타 접속 정보', 99, TRUE);

-- LINK_CATEGORY
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('LINK_CATEGORY', 'INFRAEYE', 'Infraeye(공통)', 'Infraeye(공통) 관련 링크', 1, TRUE),
('LINK_CATEGORY', 'INFRAEYE1', 'Infraeye 1', 'Infraeye 1 관련 링크', 2, TRUE),
('LINK_CATEGORY', 'INFRAEYE2', 'Infraeye 2', 'Infraeye 2 관련 링크', 3, TRUE),
('LINK_CATEGORY', 'INFRASTRUCTURE', '인프라', '인프라 관련 링크', 4, TRUE),
('LINK_CATEGORY', 'TEAM-MANAGEMENT', '팀 관리 및 운영', '팀 내 공유 정보 링크', 5, TRUE),
('LINK_CATEGORY', 'ETC', '기타', '기타 링크', 99, TRUE);

-- LINK_SUBCATEGORY
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('LINK_SUBCATEGORY', 'NOTION', '노션', '노션 문서 링크', 1, TRUE),
('LINK_SUBCATEGORY', 'SHARED-EXCEL', '공유 엑셀', '공유 엑셀 문서 링크', 2, TRUE),
('LINK_SUBCATEGORY', 'ETC', '기타', '기타 링크', 99, TRUE);

-- FILE_SYNC_STATUS
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SYNC_STATUS', 'SYNCED', '동기화됨', '파일과 DB 데이터가 일치합니다', 1, TRUE),
('FILE_SYNC_STATUS', 'UNREGISTERED', '미등록', '파일 정보가 DB에 존재하지 않습니다)', 2, TRUE),
('FILE_SYNC_STATUS', 'FILE_MISSING', '파일 없음', 'DB에는 데이터가 존재하지만 실제 파일이 없습니다', 3, TRUE),
('FILE_SYNC_STATUS', 'SIZE_MISMATCH', '크기 불일치', '실제 파일 크기가 DB 데이터와 다릅니다', 4, TRUE),
('FILE_SYNC_STATUS', 'CHECKSUM_MISMATCH', '체크섬 불일치', '파일 체크섬이 DB 데이터와 다릅니다', 5, TRUE);

-- FILE_SYNC_TARGET
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SYNC_TARGET', 'RELEASE_FILE', '릴리즈 파일', '버전별 릴리즈 파일 (versions 경로)', 1, TRUE),
('FILE_SYNC_TARGET', 'RESOURCE_FILE', '리소스 파일', '공용 리소스 파일 (resource 경로)', 2, TRUE),
('FILE_SYNC_TARGET', 'BACKUP_FILE', '백업 파일', '백업 파일 (job 경로)', 3, TRUE);

-- FILE_SYNC_ACTION
INSERT INTO code (code_type_id, code_id, code_name, description, sort_order, is_enabled) VALUES
('FILE_SYNC_ACTION', 'REGISTER', '파일 정보 등록', '미등록 파일을 DB에 등록합니다', 1, TRUE),
('FILE_SYNC_ACTION', 'UPDATE_METADATA', '파일 정보 갱신', '실제 파일 정보로 DB 메타데이터를 갱신합니다', 2, TRUE),
('FILE_SYNC_ACTION', 'DELETE_METADATA', '파일 정보 삭제', 'DB에서 메타데이터 레코드를 삭제합니다', 3, TRUE),
('FILE_SYNC_ACTION', 'DELETE_FILE', '파일 삭제', '파일시스템에서 파일을 삭제합니다', 4, TRUE),
('FILE_SYNC_ACTION', 'IGNORE', '분석 제외', '이 항목을 분석 제외 목록에 추가합니다', 5, TRUE);

-- =========================================================
-- account 테이블
-- =========================================================

INSERT INTO account (account_name, email, password, role, status) VALUES
('시스템 관리자','admin@tscientific.co.kr', '$2a$10$l8sMjsX460lFokTzvBuBOefMU0u//xpEzNCV4uhLvr0huqUWpTYPe', 'ADMIN', 'ACTIVE'),
('기본 사용자','m_user@tscientific.co.kr', '$2a$10$l8sMjsX460lFokTzvBuBOefMU0u//xpEzNCV4uhLvr0huqUWpTYPe', 'USER', 'ACTIVE');

-- =========================================================
-- department 테이블
-- =========================================================

INSERT INTO department (department_name, description) VALUES
('인프라기술팀', '인프라 기술 지원'),
('서비스기술팀', '서비스 기술 지원'),
('보안기술팀', '보안 기술 지원');

-- =========================================================
-- engineer 테이블
-- =========================================================

INSERT INTO engineer (engineer_email, position, engineer_name, department_id) VALUES
('shinss@tscientific.co.kr', '부장','신성수', 1),
('yhkim0144@tscientific.co.kr', '과장','김요한', 1),
('skykimtw@tscientific.co.kr', '과장','김태우', 1),
('choi7733@tscientific.co.kr', '과장','최은빈', 1),
('thdrudcks97@tscientific.co.kr', '대리','송경찬', 1),
('swngh56@tscientific.co.kr', '사원','신주호', 1),
('tjddyd3050@tscientific.co.kr', '사원', '최성용', 1),
('yeonhyuck@tscientific.co.kr', '사원', '최연혁', 1),
('yoonss@tscientific.co.kr', '이사', '윤성식', 2),
('eu@tscientific.co.kr', '대리', '은지영', 2),
('wychoi@tscientific.co.kr', '대리', '최우열', 2),
('kchh3617@tscientific.co.kr', '대리', '권찬혁', 2),
('swoun4221@tscientific.co.kr', '사원', '신운성', 2),
('rlaud95@tscientific.co.kr', '사원', '손기명', 2),
('ljk0105@tscientific.co.kr', '사원', '이정규', 2),
('jmkim@tscientific.co.kr', '차장', '김정목', 3),
('ssyang9417@tscientific.co.kr', '과장', '송병준', 3),
('dydtls888@tscientific.co.kr', '사원', '박용신', 3),
('twins827@tscientific.co.kr', '사원', '박준성', 3),
('tngur317@tscientific.co.kr', '사원', '오수혁', 3),
('oyj961212@tscientific.co.kr', '사원', '오유준', 3),
('yoonchul.lee@tscientific.co.kr', '사원', '이윤철', 3),
('lkh2433@tscientific.co.kr', '사원', '이경호', 3),
('pjh@tscientific.co.kr', '사원', '박재호', 3);

-- =========================================================
-- customer 테이블
-- =========================================================

INSERT INTO customer (created_by, customer_code, customer_name, description, is_active, updated_by) VALUES
('m_user@tscientific.co.kr', 'customerA', 'A회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerB', 'B회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerC', 'C회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerD', 'D회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerE', 'E회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerF', 'F회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerG', 'G회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerH', 'H회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerI', 'I회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerJ', 'J회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerK', 'K회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerL', 'L회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerM', 'M회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerN', 'N회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerO', 'O회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerP', 'P회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerQ', 'Q회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerR', 'R회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerS', 'S회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerT', 'T회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerU', 'U회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerV', 'V회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerW', 'W회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerX', 'X회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerY', 'Y회사', NULL, true, 'm_user@tscientific.co.kr'),
('m_user@tscientific.co.kr', 'customerZ', 'Z회사', NULL, true, 'm_user@tscientific.co.kr');

-- =========================================================
-- project 테이블
-- =========================================================

INSERT INTO project (project_id, project_name, description, created_by) VALUES
('infraeye1', 'Infraeye 1', 'Infraeye 1.0', 'SYSTEM'),
('infraeye2', 'Infraeye 2', 'Infraeye 2.0', 'SYSTEM');

-- =========================================================
-- customer_project 테이블
-- =========================================================

INSERT INTO customer_project (customer_id, project_id)
SELECT customer_id, 'infraeye2'
FROM customer
WHERE customer_code IN (
    'customerA', 'customerB', 'customerC', 'customerD', 'customerE', 'customerF',
    'customerG', 'customerH', 'customerI', 'customerJ', 'customerK', 'customerL',
    'customerM', 'customerN', 'customerO', 'customerP', 'customerQ', 'customerR',
    'customerS', 'customerT', 'customerU', 'customerV', 'customerW', 'customerX',
    'customerY', 'customerZ'
);

-- =========================================================
-- release_version 테이블
-- =========================================================

INSERT INTO release_version (project_id, release_type, release_category, customer_id, version, major_version, minor_version, patch_version, is_approved, approved_by, approved_at, created_by, comment, created_at) VALUES
('infraeye2', 'STANDARD', 'INSTALL', NULL, '1.0.0', 1, 0, 0, TRUE, 'jhlee@tscientific.co.kr', '2025-01-01 00:00:00', 'jhlee@tscientific.co.kr', '최초 설치본', '2025-01-01 00:00:00'),
('infraeye2', 'STANDARD', 'PATCH', NULL, '1.1.0', 1, 1, 0, TRUE, 'jhlee@tscientific.co.kr', '2025-12-18 00:00:00', 'jhlee@tscientific.co.kr', 'SMS 추가 및 그에 따른 기존 DB 변경', '2025-12-18 00:00:00');

-- =========================================================
-- release_file 테이블
-- =========================================================
INSERT INTO release_file (release_version_id,file_type,file_category,sub_category,file_name,file_path,relative_path,file_size,checksum,execution_order,description) VALUES
(1,'PDF','ETC',NULL,'Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf','versions/infraeye2/standard/1.0.x/1.0.0/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf','/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',2727778,'b281accec63e4126ca6d76aa918397387e3a5247b45788d63608b9357be3cca8',1,'설치 가이드 문서'),
(1,'MD','ETC',NULL,'설치본정보.md','versions/infraeye2/standard/1.0.x/1.0.0/install/설치본정보.md','/install/설치본정보.md',759,'768c8dabdcdaccbc5a507e09ce8b1f5072aede6db38c7aef0212866af99d5634',2,'설치본 정보'),
(2,'SQL','DATABASE','MARIADB','1.patch_mariadb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/1.patch_mariadb_ddl.sql','/database/MARIADB/1.patch_mariadb_ddl.sql',39108,'def68e585ad8f9a4eef6bf50850cb21b25396b596d1404f70010a3745f4e1b8b',1,'DDL 변경'),
(2,'SQL','DATABASE','MARIADB','2.patch_mariadb_view.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/2.patch_mariadb_view.sql','/database/MARIADB/2.patch_mariadb_view.sql',10394,'5956bb62c11d8e97231c65034d17e3d2203f990fe705b289178abb22d523b430',2,'View 변경'),
(2,'SQL','DATABASE','MARIADB','3.patch_mariadb_데이터코드.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/3.patch_mariadb_데이터코드.sql','/database/MARIADB/3.patch_mariadb_데이터코드.sql',135601,'cc373c6e9565020c4037b30dd495f1c6847ca476f8b51a2f2806fa00b0f56d48',3,'데이터 코드 추가'),
(2,'SQL','DATABASE','MARIADB','4.patch_mariadb_메뉴코드.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/4.patch_mariadb_메뉴코드.sql','/database/MARIADB/4.patch_mariadb_메뉴코드.sql',24808,'cbd7a3477ee4ca78e89ab58cce49513714b2186b14452dad09141409cb4e0b36',4,'메뉴 코드 추가'),
(2,'SQL','DATABASE','MARIADB','5.patch_mariadb_이벤트코드.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/5.patch_mariadb_이벤트코드.sql','/database/MARIADB/5.patch_mariadb_이벤트코드.sql',38804,'661682b37b7db9f3421bf291f8818290423991fb6b0e3ffef993fa03f7c6095b',5,'이벤트 코드 추가'),
(2,'SQL','DATABASE','MARIADB','6.patch_mariadb_성능지표.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/6.patch_mariadb_성능지표.sql','/database/MARIADB/6.patch_mariadb_성능지표.sql',117788,'d8b0961ec508332ee6f73c914061d1294630c7cb1c181a7cf108745ed3aa9f4a',6,'성능지표 추가'),
(2,'SQL','DATABASE','MARIADB','7.patch_mariadb_procedure.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/7.patch_mariadb_procedure.sql','/database/MARIADB/7.patch_mariadb_procedure.sql',21567,'63df6fdaa440d69abff8866b630ddea824d4a6f21d0adadf2275462250e1c804',7,'Procedure 변경'),
(2,'SQL','DATABASE','MARIADB','8.patch_mariadb_dml.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/8.patch_mariadb_dml.sql','/database/MARIADB/8.patch_mariadb_dml.sql',42238,'4cde42002ea4b62b3977e51f5f0bb3a109f8084a115f219d4297ac8a57e8e0db',8,'DML 변경'),
(2,'SQL','DATABASE','CRATEDB','1.patch_cratedb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/CRATEDB/1.patch_cratedb_ddl.sql','/database/CRATEDB/1.patch_cratedb_ddl.sql',19718,'b04cad39e4ae870c38c5472e63d6824d63d44545f3da1a5c9cd07b4db28cb2b0',1,'CrateDB DDL 변경');

-- =========================================================
-- release_version_hierarchy 테이블
-- =========================================================

-- 1.0.0 (release_version_id = 1)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES
(1, 1, 0);

-- 1.1.0 (release_version_id = 2)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES
(2, 2, 0),
(1, 2, 1);

-- =========================================================
-- resource_file 테이블 (resource_file_name 추가)
-- =========================================================

INSERT INTO resource_file (file_type, file_category, sub_category, resource_file_name, file_name, file_path, file_size, description, sort_order, created_by) VALUES
-- SCRIPT - MARIADB (sort_order: 1, 2)
('SH', 'SCRIPT', 'MARIADB', 'MariaDB 백업', 'mariadb_backup.sh', 'resource/script/MARIADB/mariadb_backup.sh', 11025, 'MariaDB 데이터베이스 백업 수행 셸 스크립트', 1, 'system'),
('SH', 'SCRIPT', 'MARIADB', 'MariaDB 복원', 'mariadb_restore.sh', 'resource/script/MARIADB/mariadb_restore.sh', 12655, 'MariaDB 데이터베이스 복원 수행 셸 스크립트', 2, 'system'),

-- SCRIPT - CRATEDB (sort_order: 1, 2)
('SH', 'SCRIPT', 'CRATEDB', 'CrateDB 백업', 'cratedb_backup.sh', 'resource/script/CRATEDB/cratedb_backup.sh', 11458, 'CrateDB 데이터베이스 스냅샷 생성 셸 스크립트', 1, 'system'),
('SH', 'SCRIPT', 'CRATEDB', 'CrateDB 복원', 'cratedb_restore.sh', 'resource/script/CRATEDB/cratedb_restore.sh', 14675, 'CrateDB 데이터베이스 복원 셸 스크립트', 2, 'system'),

-- DOCUMENT - INFRAEYE2 (sort_order: 1)
('PDF', 'DOCUMENT', 'INFRAEYE2', 'Infraeye2 설치 가이드 문서', 'Infraeye2 설치가이드(OracleLinux8.6).pdf', 'resource/document/INFRAEYE2/Infraeye2 설치가이드(OracleLinux8.6).pdf', 2727778, 'Infraeye2 설치 상세 가이드 문서', 1, 'system');

-- =========================================================
-- resource_link 테이블
-- =========================================================

INSERT INTO resource_link (link_category,sub_category,link_name,link_url,description,sort_order,created_by) VALUES
('TEAM-MANAGEMENT','NOTION','개발 2팀 페이지','https://www.notion.so/tscientific/2-c32cc14be7904787a6acb88e8106edaf?source=copy_link','개발 2팀 노션 페이지',1,'admin@tscientific.co.kr'),
('INFRAEYE2','SHARED-EXCEL','Infraeye 2 메뉴관리','https://itnomads.sharepoint.com/:x:/r/sites/TS_NMS/_layouts/15/Doc2.aspx?action=edit&sourcedoc=%7B205c004f-cbe4-4296-8a32-3c39bf08e8af%7D&wdOrigin=TEAMS-MAGLEV.undefined_ns.rwc&wdExp=TEAMS-TREATMENT&wdhostclicktime=1748848209807&web=1','관리대상: 메뉴, 역할별메뉴, 메뉴기능 기초데이터',1,'admin@tscientific.co.kr'),
('INFRAEYE2','SHARED-EXCEL','Infraeye 2 이벤트코드 관리','https://itnomads.sharepoint.com/:x:/r/sites/TS_NMS/_layouts/15/Doc2.aspx?action=edit&sourcedoc=%7B11a03087-ecbc-4ae0-8b3e-4352edbdfc55%7D&wdOrigin=TEAMS-MAGLEV.teamsSdk_ns.rwc&wdExp=TEAMS-TREATMENT&wdhostclicktime=1754539574091&web=1','관리대상: 이벤트코드종류, 이벤트코드, 성능지표, 성능지표(SMS)',2,'admin@tscientific.co.kr'),
('INFRAEYE2','SHARED-EXCEL','Infraeye 2 데이터코드 관리','https://itnomads.sharepoint.com/:x:/r/sites/TS_NMS/_layouts/15/Doc2.aspx?action=edit&sourcedoc=%7Be486a6da-e310-41a8-8df4-5ac94669f595%7D&wdOrigin=TEAMS-MAGLEV.teamsSdk_ns.rwc&wdExp=TEAMS-TREATMENT&wdhostclicktime=1754539517973&web=1','관리대상: 데이터코드분류, 데이터코드',3,'admin@tscientific.co.kr'),
('INFRAEYE2','NOTION','SMS 개발 공유 문서','https://www.notion.so/tscientific/RnD-InfraEye-SMS-PJT-17e23133945280bf887dc363f43851b9','',4,'admin@tscientific.co.kr');


-- =========================================================
-- menu 테이블
-- =========================================================

-- 1depth 메뉴
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('version_management', '버전 관리', NULL, '릴리즈 버전 관리', TRUE, FALSE, 1),
('patch_management', '패치 관리', NULL, '패치 파일 관리', TRUE, FALSE, 2),
('operation_management', '운영 관리', NULL, '릴리즈 매니저 운영에 필요한 데이터를 관리합니다.', TRUE, FALSE, 3),
('development_support', '개발 지원', NULL, '인프라 서비스 및 원격 작업 등의 편의 기능을 제공합니다.', TRUE, FALSE, 4);

-- 2depth 메뉴 - 버전 관리
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('version_standard', 'Standard', 'releases/standard', '표준 버전을 관리합니다.', FALSE, TRUE, 1),
('version_custom', 'Custom', 'releases/custom', '커스텀 사이트 버전을 관리합니다.', FALSE, TRUE, 2);

-- 2depth 메뉴 - 패치 관리
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('patch_standard', 'Standard', 'patches/standard', '표준 패치를 관리합니다.', FALSE, TRUE, 1),
('patch_custom', 'Custom', 'patches/custom', '커스텀 사이트 패치를 관리합니다.', FALSE, TRUE, 2);

-- 2depth 메뉴 - 운영 관리
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('operation_customers', '고객사', 'operations/customers', '고객사 정보를 관리합니다.', TRUE, FALSE, 1),
('operation_engineers', '엔지니어', 'operations/engineers', '엔지니어 정보를 관리합니다.', TRUE, FALSE, 2),
('operation_filesync', '파일 동기화', 'operations/file-sync', '실제 파일과 DB 메타데이터 간 불일치를 분석하고 동기화합니다.', TRUE, FALSE, 3),
('operation_projects', '프로젝트', 'operations/projects', '프로젝트 정보를 관리합니다.', TRUE, FALSE, 4),
('operation_accounts', '계정', 'operations/accounts', '계정 정보를 관리합니다.', TRUE, FALSE, 5);


-- 2depth 메뉴 - 개발 지원
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('remote_jobs', '원격 작업', NULL, '원격 작업 서비스를 제공합니다.', FALSE, FALSE, 1),
('infrastructure', '인프라', NULL, '개발 인프라 관련 정보를 관리 및 제공합니다.', FALSE, FALSE, 2);

-- 3depth 메뉴 - 원격 작업
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('remote_mariadb', 'MariaDB', 'development-support/remote-jobs/mariadb', 'MariaDB 백업 및 복원 기능을 제공합니다.', TRUE, FALSE, 1),
('remote_terminal', '터미널', 'development-support/remote-jobs/terminal', 'SSH, SFTP 기능이 있는 웹 터미널을 제공합니다.', TRUE, FALSE, 2);

-- 3depth 메뉴 - 인프라
INSERT INTO menu (menu_id, menu_name, menu_url, description, is_description_visible, is_line_break, menu_order) VALUES
('infrastructure_resources', '리소스', 'development-support/infrastructure/resources', '리소스 정보를 관리 및 제공합니다.', TRUE, FALSE, 1),
('infrastructure_services', '서비스', 'development-support/infrastructure/service', '서비스 정보를 관리 및 제공합니다.', TRUE, FALSE, 2);

-- =========================================================
-- menu_hierarchy 테이블
-- =========================================================

-- 1depth 메뉴 (자기 자신)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('version_management', 'version_management', 0),
('patch_management', 'patch_management', 0),
('operation_management', 'operation_management', 0),
('development_support', 'development_support', 0);

-- 2depth 메뉴 (자기 자신)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('version_standard', 'version_standard', 0),
('version_custom', 'version_custom', 0),
('patch_standard', 'patch_standard', 0),
('patch_custom', 'patch_custom', 0),
('operation_customers', 'operation_customers', 0),
('operation_engineers', 'operation_engineers', 0),
('operation_filesync', 'operation_filesync', 0),
('operation_projects', 'operation_projects', 0),
('operation_accounts', 'operation_accounts', 0),
('remote_jobs', 'remote_jobs', 0),
('infrastructure', 'infrastructure', 0);;

-- 3depth 메뉴 (자기 자신)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('remote_mariadb', 'remote_mariadb', 0),
('remote_terminal', 'remote_terminal', 0),
('infrastructure_resources', 'infrastructure_resources', 0),
('infrastructure_services', 'infrastructure_services', 0);

-- 부모-자식 관계 (depth=1) - 버전 관리
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('version_management', 'version_standard', 1),
('version_management', 'version_custom', 1);

-- 부모-자식 관계 (depth=1) - 패치 관리
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('patch_management', 'patch_standard', 1),
('patch_management', 'patch_custom', 1);

-- 부모-자식 관계 (depth=1) - 운영 관리
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('operation_management', 'operation_customers', 1),
('operation_management', 'operation_engineers', 1),
('operation_management', 'operation_filesync', 1),
('operation_management', 'operation_projects', 1),
('operation_management', 'operation_accounts', 1);

-- 부모-자식 관계 (depth=1) - 개발 지원
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('development_support', 'remote_jobs', 1),
('development_support', 'infrastructure', 1);

-- 부모-자식 관계 (depth=1) - 원격 작업
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('remote_jobs', 'remote_mariadb', 1),
('remote_jobs', 'remote_terminal', 1);

-- 부모-자식 관계 (depth=1) - 인프라
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('infrastructure', 'infrastructure_resources', 1),
('infrastructure', 'infrastructure_services', 1);

-- 조상-손자 관계 (depth=2) - 개발 지원 > 원격 작업 > MariaDB/터미널
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('development_support', 'remote_mariadb', 2),
('development_support', 'remote_terminal', 2);

-- 조상-손자 관계 (depth=2) - 개발 지원 > 인프라 > 리소스/서비스
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('development_support', 'infrastructure_resources', 2),
('development_support', 'infrastructure_services', 2);

-- =========================================================
-- menu_role 테이블
-- =========================================================

-- ADMIN: 모든 메뉴 접근 가능
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth
('version_management', 'ADMIN'),
('patch_management', 'ADMIN'),
('operation_management', 'ADMIN'),
('development_support', 'ADMIN'),
-- 2depth - 버전 관리
('version_standard', 'ADMIN'),
('version_custom', 'ADMIN'),
-- 2depth - 패치 관리
('patch_standard', 'ADMIN'),
('patch_custom', 'ADMIN'),
-- 2depth - 운영 관리
('operation_customers', 'ADMIN'),
('operation_engineers', 'ADMIN'),
('operation_filesync', 'ADMIN'),
('operation_projects', 'ADMIN'),
('operation_accounts', 'ADMIN'),
-- 2depth - 개발 지원
('remote_jobs', 'ADMIN'),
('infrastructure', 'ADMIN'),
-- 3depth - 원격 작업
('remote_mariadb', 'ADMIN'),
('remote_terminal', 'ADMIN'),
-- 3depth - 인프라
('infrastructure_resources', 'ADMIN'),
('infrastructure_services', 'ADMIN');

-- USER: 계정 메뉴 제외
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth
('version_management', 'USER'),
('patch_management', 'USER'),
('operation_management', 'USER'),
('development_support', 'USER'),
-- 2depth - 버전 관리
('version_standard', 'USER'),
('version_custom', 'USER'),
-- 2depth - 패치 관리
('patch_standard', 'USER'),
('patch_custom', 'USER'),
-- 2depth - 운영 관리 (계정 제외)
('operation_customers', 'USER'),
('operation_engineers', 'USER'),
('operation_filesync', 'USER'),
('operation_projects', 'USER'),
-- 2depth - 개발 지원
('remote_jobs', 'USER'),
('infrastructure', 'USER'),
-- 3depth - 원격 작업
('remote_mariadb', 'USER'),
('remote_terminal', 'USER'),
-- 3depth - 인프라
('infrastructure_resources', 'USER'),
('infrastructure_services', 'USER');

-- GUEST: 운영 관리 전체 제외
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth (운영 관리 제외)
('version_management', 'GUEST'),
('patch_management', 'GUEST'),
('development_support', 'GUEST'),
-- 2depth - 버전 관리
('version_standard', 'GUEST'),
('version_custom', 'GUEST'),
-- 2depth - 패치 관리
('patch_standard', 'GUEST'),
('patch_custom', 'GUEST'),
-- 2depth - 개발 지원
('remote_jobs', 'GUEST'),
('infrastructure', 'GUEST'),
-- 3depth - 원격 작업
('remote_mariadb', 'GUEST'),
('remote_terminal', 'GUEST'),
-- 3depth - 인프라
('infrastructure_resources', 'GUEST'),
('infrastructure_services', 'GUEST');

-- =========================================================
-- service 테이블
-- =========================================================
INSERT INTO service (service_name,service_type,description,sort_order,is_active,created_by) VALUES
 ('infraeye 1 (common)','infraeye1','infraeye 1 공용 서비스',1,1,'admin@tscientific.co.kr'),
 ('infraeye 2 (dev)','infraeye2','infraeye 2 개발',2,1,'admin@tscientific.co.kr'),
 ('infraeye 2 (test)','infraeye2','infraeye 2 테스트',3,1,'admin@tscientific.co.kr'),
 ('gitea','infra','git 저장소',1,1,'admin@tscientific.co.kr'),
 ('jenkins','infra','gitea 연동 CI/CD',2,1,'admin@tscientific.co.kr'),
 ('NAS','infra','NAS 서버',3,1,'admin@tscientific.co.kr'),
 ('gitlab','infra','git 저장소 (release_manager)',4,1,'admin@tscientific.co.kr'),
 ('harbor','infra','도커 이미지 저장소 (private registry)',5,1,'admin@tscientific.co.kr'),
 ('infraeye 2 (common)','infraeye2','infraeye 2 공용 서비스',4,1,'admin@tscientific.co.kr'),
 ('release-manager','infra','제품 관리 솔루션',6,1,'admin@tscientific.co.kr');

INSERT INTO service_component (service_id,component_type,component_name,host,port,url,account_id,password,ssh_port,ssh_account_id,ssh_password,description,sort_order,is_active,created_by) VALUES
 (6,'WEB','nas - web','10.110.1.99',5000,'http://10.110.1.99','admin','VFzudy/OvPjp4GT91ZkHjciyj7/EVDo9mZWtQKCZxHM=',NULL,NULL,NULL,'NAS 서버',1,1,'admin@tscientific.co.kr'),
 (5,'WEB','jenkins - web','10.110.1.105',38080,'http://10.110.1.105:38080','admin','L7Ol4qrBfB1PyVceZMBo1Vhd7ORIMsAqkuLZZdnIATI=',NULL,NULL,NULL,'Jenkins - web',1,1,'admin@tscientific.co.kr'),
 (4,'WEB','gitea - web','10.110.1.99',3000,'http://10.110.1.99:3000',NULL,NULL,NULL,NULL,NULL,'gitea - web',1,1,'admin@tscientific.co.kr'),
 (1,'WEB','infraeye1 - web','10.110.1.104',60000,'https://10.110.1.104:60000','m_user','wByhfewFrYAgEXpTejpd5ZXGJ13zq+bh+c44IupMvuM=',20022,'root','+5j99UFl89RKCXLI7umAH2Vh4BnPLBW+FdArBBST2aM=','infraeye 1 개발',1,1,'admin@tscientific.co.kr'),
 (2,'WEB','infraeye2 - web','10.110.1.103',60000,'http://10.110.1.103:60000','m_user','cgxjQSEv4hPgFtqvYNZF57RuCtRawRmop1eQ+TfR7/I=',20022,'root','3Q/XUhl1UozSKVaFc0KS5on+VkZjsrXzBBE57zkkFEo=','infraeye2 개발 서버',1,1,'admin@tscientific.co.kr'),
 (2,'DATABASE','infraeye2 - mariadb','10.110.1.103',13306,NULL,'infraeye','87QvTx21kwVeBWtsku5TNvkttDm3JonQYkjlHO0Klu4=',NULL,NULL,NULL,'infraeye 2 개발서버 MariaDB',2,1,'admin@tscientific.co.kr'),
 (1,'DATABASE','infraeye1 - mariadb','10.110.1.104',3306,NULL,'infraeye','QzLtMAyM1yRUVTdlCLHKv7fylOy/h6IDwvRQTrtrY7k=',NULL,NULL,NULL,'infraeye 1 개발 서버 MariaDB',2,1,'admin@tscientific.co.kr'),
 (3,'WEB','infraeye 2 (test) - web','10.140.1.21',60000,'http://10.140.1.21:60000','m_user','1YnrpmbOXtT4PvEVeLzvDhBD3oEz/e+o7vfrZ622oNk=',22,'root','N4gIE1Px1zeEIrkbrhNXQBNR5xvChWo7A3DAFVbIkfg=','infraeye 2 Test서버 - Web',1,1,'admin@tscientific.co.kr'),
 (3,'DATABASE','infraeye2 (test) - mariadb','10.140.1.21',13306,NULL,'infraeye','BcGHIhwqkFThHAfnEgIq5yRIsaW0f23VIj5WxLLBg4Q=',NULL,NULL,NULL,'infraeye2 Test서버 - MariaDB',2,1,'admin@tscientific.co.kr'),
 (3,'DATABASE','infraeye2 - cratedb','10.140.1.21',15432,NULL,'infraeye','m8yxGP/rPFgb1egZqo/c8AmHkGx7nf4zufA9TdA8xd0=',NULL,NULL,NULL,'infraeye2 Test서버 - cratedb',3,1,'admin@tscientific.co.kr'),
 (3,'DATABASE','infraeye2 (test) - redis','10.140.1.21',55501,NULL,NULL,'zR4bpiQj7ghRbNn7kzQ0J11YloMHaXpZ2MG9lDt3jpQ=',NULL,NULL,NULL,'infraeye 2 Test서버 - redis',4,1,'admin@tscientific.co.kr'),
 (7,'WEB','gitlab - web','10.230.1.17',20080,'http://10.230.1.17:20080',NULL,NULL,22,'root','Ve/yxenrSnPJ8j4NLVPHOc9nDe3c91nYQSDCVyNAfG0=',NULL,1,1,'admin@tscientific.co.kr'),
 (8,'WEB','harbor - web','10.230.1.17',20081,'http://10.230.1.17:20081','admin','XQRR0NaRA/IYNk2Ai+V+VjCu5X6IGKpSSid42DuxicY=',22,'root','x9UJCXsALcpiDcbl47GWcrPZvISPF9jiFcOZNcMb92w=',NULL,1,1,'admin@tscientific.co.kr'),
 (9,'WEB','infraeye2 - web','10.110.1.101',60000,'http://10.110.1.101:60000','m_user','owkdX5IAr8h2QOKls8j+Cv/rL8lr2iRf56A03FioHOE=',20022,'infraeye','9oDVPDpA6S+lKmcF37g6iv4OvWCqQRPpboQFuBDzFgI=',NULL,1,1,'admin@tscientific.co.kr'),
 (9,'DATABASE','infraeye2 - mariadb','10.110.1.101',3306,NULL,'netcruz','GPHCilpH8Z2Ef4Al433voTro3387wa029atp5MidOe8=',NULL,NULL,NULL,NULL,2,1,'admin@tscientific.co.kr'),
 (9,'DATABASE','infraeye2 - cratedb','10.110.1.101',5432,NULL,'crate',NULL,NULL,NULL,NULL,NULL,3,1,'admin@tscientific.co.kr'),
 (2,'DATABASE','infraeye2 - cratedb','10.110.1.103',15432,NULL,'infraeye','2aCMw++VjUCDoHswe9oklMl8ixzOkgGd+MRhftU0wQE=',NULL,NULL,NULL,NULL,3,1,'admin@tscientific.co.kr'),
 (2,'DATABASE','infraeye2 - redis','10.110.1.103',55501,NULL,NULL,'aWbBM3V4Ck0gwHB4DEvM3u33JkcfxC719WSYHnzjuC8=',NULL,NULL,NULL,NULL,4,1,'admin@tscientific.co.kr'),
 (10,'WEB','10.110.1.106','10.110.1.106',13000,'http://10.110.1.106:13000','m_user@tscientific.co.kr','uxeIfWdySB+PDcViriSbqQ23yv3aXoIoi+WH15cR+gQ=',20022,'infraeye','RoH+FgnT/Z74anKLtK0mQV6YLvOfqbreBlL+Tmp3eag=','release-manager - web',1,1,'admin@tscientific.co.kr'),
 (10,'WEB','release-manager - api','10.110.1.106',18080,'http://10.110.1.106:13000','m_user@tscientific.co.kr','uxeIfWdySB+PDcViriSbqQ23yv3aXoIoi+WH15cR+gQ=',NULL,NULL,NULL,'release-manager - api',2,1,'admin@tscientific.co.kr'),
 (10,'DATABASE','release-manager - mariadb','10.110.1.106',13306,NULL,'root','3exvWPAkRx6FNwpupjQmP4bSafqe3fAj9ny11kueQ24=',NULL,NULL,NULL,'release-manager - mariadb',3,1,'admin@tscientific.co.kr'),
 (10,'DATABASE','release-manager - redis','10.110.1.106',16379,NULL,NULL,'JLiOIBYP9Wc1zie8Bcvfjq/2Q0YvpUhjR/wYxE1rFvg=',NULL,NULL,NULL,'release-manager - redis',4,1,'admin@tscientific.co.kr');
