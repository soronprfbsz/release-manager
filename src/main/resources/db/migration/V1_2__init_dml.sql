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
('COMPONENT_TYPE', '컴포넌트 유형', '서비스 컴포넌트(접속 정보) 유형', TRUE);

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

INSERT INTO release_version (project_id, release_type, release_category, customer_id, version, major_version, minor_version, patch_version, created_by, comment, created_at) VALUES
('infraeye2', 'STANDARD', 'INSTALL', NULL, '1.0.0', 1, 0, 0, 'TS', '최초 설치본', '2025-01-01 00:00:00'),
('infraeye2', 'STANDARD', 'PATCH', NULL, '1.1.0', 1, 1, 0, 'jhlee@tscientific', '데이터코드, 이벤트코드, 메뉴코드 추가 / SMS 기능 추가 / VERSION_HISTORY 테이블 추가 / V_INFO_MCH 관련 뷰 변경', '2025-10-31 00:00:00'),
('infraeye2', 'STANDARD', 'PATCH', NULL, '1.1.1', 1, 1, 1, 'jhlee@tscientific', 'SMS - 운영관리 - 파일 기능 관련 테이블 추가', '2025-11-05 00:00:00'),
('infraeye2', 'STANDARD', 'PATCH', NULL, '1.1.2', 1, 1, 2, 'jhlee@tscientific', 'SMS - 로그관리 - 로그 모니터 정책 상세 테이블 추가', '2025-11-25 00:00:00'),
('infraeye2', 'STANDARD', 'PATCH', NULL, '1.1.3', 1, 1, 3, 'jhlee@tscientific' ,'SMS 파일, 로그, 서비스 개발 기능 추가에 따른 패치본', '2025-12-16 12:16:49');

-- =========================================================
-- release_file 테이블
-- =========================================================
INSERT INTO release_file (release_version_id,file_type,file_category,sub_category,file_name,file_path,relative_path,file_size,checksum,execution_order,description) VALUES
(1,'PDF','ETC',NULL,'Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf','versions/infraeye2/standard/1.0.x/1.0.0/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf','/install/Infraeye2 설치가이드(OracleLinux8.6)_NEW.pdf',2727778,'4e641f7d25bbaa0061f553b92ef3d9e9',1,'설치 가이드 문서'),
(1,'MD','ETC',NULL,'설치본정보.md','versions/infraeye2/standard/1.0.x/1.0.0/install/설치본정보.md','/install/설치본정보.md',778,'8e5adf2b877090de4f3ec5739f71216c',2,'설치본 정보'),
(2,'SQL','DATABASE','MARIADB','1.patch_mariadb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/1.patch_mariadb_ddl.sql','/database/MARIADB/1.patch_mariadb_ddl.sql',34879,'f8b9f64345555c9a4a9c9101aaa8b701',1,'DDL 변경'),
(2,'SQL','DATABASE','MARIADB','2.patch_mariadb_view.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/2.patch_mariadb_view.sql','/database/MARIADB/2.patch_mariadb_view.sql',10742,'6735c7267bedc684f155ce05eaa5b7df',2,'View 변경'),
(2,'SQL','DATABASE','MARIADB','3.patch_mariadb_데이터코드.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/3.patch_mariadb_데이터코드.sql','/database/MARIADB/3.patch_mariadb_데이터코드.sql',134540,'faec479bf1582dfb20199fdd468676f7',3,'데이터 코드 추가'),
(2,'SQL','DATABASE','MARIADB','4.patch_mariadb_이벤트코드.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/4.patch_mariadb_이벤트코드.sql','/database/MARIADB/4.patch_mariadb_이벤트코드.sql',36847,'e2e818dfa626c93894b5774badee0219',4,'이벤트 코드 추가'),
(2,'SQL','DATABASE','MARIADB','5.patch_mariadb_메뉴코드.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/5.patch_mariadb_메뉴코드.sql','/database/MARIADB/5.patch_mariadb_메뉴코드.sql',25144,'3eb290c91cf66dacbc02a746bec2bef0',5,'메뉴 코드 추가'),
(2,'SQL','DATABASE','MARIADB','6.patch_mariadb_procedure.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/6.patch_mariadb_procedure.sql','/database/MARIADB/6.patch_mariadb_procedure.sql',22183,'25942f2c2201629efcc333278f8eac38',6,'Procedure 변경'),
(2,'SQL','DATABASE','MARIADB','7.patch_mariadb_dml.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/MARIADB/7.patch_mariadb_dml.sql','/database/MARIADB/7.patch_mariadb_dml.sql',37330,'3fa1ec88b5a638fb6d67a41119d61854',7,'DML 변경'),
(2,'SQL','DATABASE','CRATEDB','1.patch_cratedb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.0/database/CRATEDB/1.patch_cratedb_ddl.sql','/database/CRATEDB/1.patch_cratedb_ddl.sql',19363,'1b68614d70c52cade269e5accca724d5',1,'CrateDB DDL 변경'),
(3,'SQL','DATABASE','MARIADB','1.patch_mariadb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.1/database/MARIADB/1.patch_mariadb_ddl.sql','/database/MARIADB/1.patch_mariadb_ddl.sql',4867,'848ecec66ce257e0fcec4088294c816d',1,'파일 기능 관련 DDL 추가'),
(3,'SQL','DATABASE','MARIADB','2.patch_mariadb_dml.sql','versions/infraeye2/standard/1.1.x/1.1.1/database/MARIADB/2.patch_mariadb_dml.sql','/database/MARIADB/2.patch_mariadb_dml.sql',660,'63fe833edd62599db2ce8c758eae0240',2,'파일 기능 관련 DML 추가'),
(4,'SQL','DATABASE','MARIADB','1.patch_mariadb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.2/database/MARIADB/1.patch_mariadb_ddl.sql','/database/MARIADB/1.patch_mariadb_ddl.sql',1765,'48bb04f6b3f2f4560ab42c0c37fcacbc',1,'SMS 로그 모니터링 정책 상세 테이블 추가'),
(5,'SQL','DATABASE','CRATEDB','1.patch_cratedb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/CRATEDB/1.patch_cratedb_ddl.sql','/database/CRATEDB/1.patch_cratedb_ddl.sql',784,'84b4866515120b4516284d028443ee8b',1,'ZIP 파일 업로드로 생성된 데이터베이스 파일'),
(5,'SQL','DATABASE','MARIADB','1.patch_mariadb_ddl.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/MARIADB/1.patch_mariadb_ddl.sql','/database/MARIADB/1.patch_mariadb_ddl.sql',172,'cedcae240caa7d88d54cd409be8f7287',1,'ZIP 파일 업로드로 생성된 데이터베이스 파일'),
(5,'SQL','DATABASE','MARIADB','2.patch_mariadb_데이터코드.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/MARIADB/2.patch_mariadb_데이터코드.sql','/database/MARIADB/2.patch_mariadb_데이터코드.sql',137161,'56e4688326cf1ad0b67647521d259b8e',2,'데이터 코드 추가'),
(5,'SQL','DATABASE','MARIADB','3.patch_mariadb_이벤트코드.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/MARIADB/3.patch_mariadb_이벤트코드.sql','/database/MARIADB/3.patch_mariadb_이벤트코드.sql',38804,'6d06d29b77f127e616a3a743f9d6c59c',3,'이벤트 코드 추가'),
(5,'SQL','DATABASE','MARIADB','4.patch_mariadb_메뉴코드.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/MARIADB/4.patch_mariadb_메뉴코드.sql','/database/MARIADB/4.patch_mariadb_메뉴코드.sql',25146,'f5214766ea4845f9c37b67e332766394',4,'메뉴 코드 추가'),
(5,'SQL','DATABASE','MARIADB','5.patch_mariadb_성능지표.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/MARIADB/5.patch_mariadb_성능지표.sql','/database/MARIADB/5.patch_mariadb_성능지표.sql',118114,'c156eeda96e93f807c3cbde00f7f50e7',5,'성능지표 추가'),
(5,'SQL','DATABASE','MARIADB','6.patch_mariadb_dml.sql','versions/infraeye2/standard/1.1.x/1.1.3/database/MARIADB/6.patch_mariadb_dml.sql','/database/MARIADB/6.patch_mariadb_dml.sql',1557,'113afe7849b95588774ebfd04acb2d43',6,'SMS 엔진 기본값 및 스케줄 추가');

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

-- 1.1.1 (release_version_id = 3)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES
(3, 3, 0),
(2, 3, 1),
(1, 3, 2);

-- 1.1.2 (release_version_id = 4)
INSERT INTO release_version_hierarchy (ancestor_id, descendant_id, depth) VALUES
(4, 4, 0),
(3, 4, 1),
(2, 4, 2),
(1, 4, 3);

-- 1.1.3 (release_version_id = 5)
INSERT INTO release_version_hierarchy (ancestor_id,descendant_id,`depth`) VALUES
(1, 5, 4),
(2, 5, 3),
(3, 5, 2),
(4, 5, 1),
(5, 5, 0);

-- =========================================================
-- resource_file 테이블 (sort_order 추가)
-- =========================================================

INSERT INTO resource_file (file_type, file_category, sub_category, file_name, file_path, file_size, description, sort_order, created_by) VALUES
-- SCRIPT - MARIADB (sort_order: 1, 2)
('SH', 'SCRIPT', 'MARIADB', 'mariadb_backup.sh', 'resource/script/MARIADB/mariadb_backup.sh', 11025, 'MariaDB 백업 스크립트', 1, 'system'),
('SH', 'SCRIPT', 'MARIADB', 'mariadb_restore.sh', 'resource/script/MARIADB/mariadb_restore.sh', 12655, 'MariaDB 복원 스크립트', 2, 'system'),

-- SCRIPT - CRATEDB (sort_order: 1, 2)
('SH', 'SCRIPT', 'CRATEDB', 'cratedb_backup.sh', 'resource/script/CRATEDB/cratedb_backup.sh', 11458, 'CrateDB 백업 스크립트', 1, 'system'),
('SH', 'SCRIPT', 'CRATEDB', 'cratedb_restore.sh', 'resource/script/CRATEDB/cratedb_restore.sh', 14675, 'CrateDB 복원 스크립트', 2, 'system'),

-- DOCUMENT - INFRAEYE2 (sort_order: 1)
('PDF', 'DOCUMENT', 'INFRAEYE2', 'Infraeye2 설치가이드(OracleLinux8.6).pdf', 'resource/document/INFRAEYE2/Infraeye2 설치가이드(OracleLinux8.6).pdf', 2727778, 'Infraeye2 설치 가이드 문서', 1, 'system');

-- =========================================================
-- menu 테이블
-- =========================================================

-- 1depth 메뉴
INSERT INTO menu (menu_id, menu_name, menu_order) VALUES
('version_management', '버전 관리', 1),
('patch_management', '패치 관리', 2),
('operation_management', '운영 관리', 3),
('job_management', '작업 관리', 4),
('resource_management', '리소스 관리', 5),
('service_management', '서비스 관리', 7);

-- 2depth 메뉴 - 버전 관리
INSERT INTO menu (menu_id, menu_name, menu_order) VALUES
('version_standard', 'Standard', 1),
('version_custom', 'Custom', 2);

-- 2depth 메뉴 - 패치 관리
INSERT INTO menu (menu_id, menu_name, menu_order) VALUES
('patch_standard', 'Standard', 1),
('patch_custom', 'Custom', 2);

-- 2depth 메뉴 - 운영 관리
INSERT INTO menu (menu_id, menu_name, menu_order) VALUES
('operation_customer', '고객사', 1),
('operation_engineer', '엔지니어', 2),
('operation_account', '계정', 3);

-- 2depth 메뉴 - 작업 관리
INSERT INTO menu (menu_id, menu_name, menu_order) VALUES
('job_mariadb', 'MariaDB', 1),
('job_terminal', '터미널', 2);

-- =========================================================
-- menu_hierarchy 테이블
-- =========================================================

-- 1depth 메뉴 (자기 자신)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('version_management', 'version_management', 0),
('patch_management', 'patch_management', 0),
('operation_management', 'operation_management', 0),
('job_management', 'job_management', 0),
('resource_management', 'resource_management', 0),
('service_management', 'service_management', 0);

-- 2depth 메뉴 (자기 자신)
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('version_standard', 'version_standard', 0),
('version_custom', 'version_custom', 0),
('patch_standard', 'patch_standard', 0),
('patch_custom', 'patch_custom', 0),
('operation_customer', 'operation_customer', 0),
('operation_engineer', 'operation_engineer', 0),
('operation_account', 'operation_account', 0),
('job_mariadb', 'job_mariadb', 0),
('job_terminal', 'job_terminal', 0);

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
('operation_management', 'operation_customer', 1),
('operation_management', 'operation_engineer', 1),
('operation_management', 'operation_account', 1);

-- 부모-자식 관계 (depth=1) - 작업 관리
INSERT INTO menu_hierarchy (ancestor, descendant, depth) VALUES
('job_management', 'job_mariadb', 1),
('job_management', 'job_terminal', 1);

-- =========================================================
-- menu_role 테이블
-- =========================================================

-- ADMIN: 모든 메뉴 접근 가능
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth
('version_management', 'ADMIN'),
('patch_management', 'ADMIN'),
('operation_management', 'ADMIN'),
('job_management', 'ADMIN'),
('resource_management', 'ADMIN'),
('service_management', 'ADMIN'),
-- 2depth - 버전 관리
('version_standard', 'ADMIN'),
('version_custom', 'ADMIN'),
-- 2depth - 패치 관리
('patch_standard', 'ADMIN'),
('patch_custom', 'ADMIN'),
-- 2depth - 운영 관리
('operation_customer', 'ADMIN'),
('operation_engineer', 'ADMIN'),
('operation_account', 'ADMIN'),
-- 2depth - 작업 관리
('job_mariadb', 'ADMIN'),
('job_terminal', 'ADMIN');

-- USER: 계정 메뉴 제외
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth
('version_management', 'USER'),
('patch_management', 'USER'),
('operation_management', 'USER'),
('job_management', 'USER'),
('resource_management', 'USER'),
('service_management', 'USER'),
-- 2depth - 버전 관리
('version_standard', 'USER'),
('version_custom', 'USER'),
-- 2depth - 패치 관리
('patch_standard', 'USER'),
('patch_custom', 'USER'),
-- 2depth - 운영 관리 (계정 제외)
('operation_customer', 'USER'),
('operation_engineer', 'USER'),
-- 2depth - 작업 관리
('job_mariadb', 'USER'),
('job_terminal', 'USER');

-- GUEST: 운영 관리 전체 제외
INSERT INTO menu_role (menu_id, role) VALUES
-- 1depth (운영 관리 제외)
('version_management', 'GUEST'),
('patch_management', 'GUEST'),
('job_management', 'GUEST'),
('resource_management', 'GUEST'),
('service_management', 'GUEST'),
-- 2depth - 버전 관리
('version_standard', 'GUEST'),
('version_custom', 'GUEST'),
-- 2depth - 패치 관리
('patch_standard', 'GUEST'),
('patch_custom', 'GUEST'),
-- 2depth - 작업 관리
('job_mariadb', 'GUEST'),
('job_terminal', 'GUEST');

-- =========================================================
-- service 테이블
-- =========================================================
INSERT INTO service (service_name,service_type,description,sort_order,is_active,created_by) VALUES
 ('infraeye 1 (dev)','infraeye1','infraeye 1 개발',1,1,'admin@tscientific.co.kr'),
 ('infraeye 2 (dev)','infraeye2','infraeye 2 개발',1,1,'admin@tscientific.co.kr'),
 ('infraeye 2 (test)','infraeye2','infraeye 2 테스트',2,1,'admin@tscientific.co.kr'),
 ('gitea','infra','git 저장소',1,1,'admin@tscientific.co.kr'),
 ('jenkins','infra','gitea 연동 CI/CD',2,1,'admin@tscientific.co.kr'),
 ('NAS','infra','NAS 서버',3,1,'admin@tscientific.co.kr');

INSERT INTO service_component (service_id,component_type,component_name,host,port,url,account_id,password,ssh_port,ssh_account_id,ssh_password,description,sort_order,is_active,created_by) VALUES
 (6,'WEB','nas - web','10.110.1.99',5000,'http://10.110.1.99','admin','VFzudy/OvPjp4GT91ZkHjciyj7/EVDo9mZWtQKCZxHM=',NULL,NULL,NULL,'NAS 서버',1,1,'admin@tscientific.co.kr'),
 (5,'WEB','jenkins - web','10.110.1.105',38080,'http://10.110.1.105:38080','admin','L7Ol4qrBfB1PyVceZMBo1Vhd7ORIMsAqkuLZZdnIATI=',NULL,NULL,NULL,'Jenkins - web',1,1,'admin@tscientific.co.kr'),
 (4,'WEB','gitea - web','10.110.1.99',3000,'http://10.110.1.99:3000',NULL,NULL,NULL,NULL,NULL,'gitea - web',1,1,'admin@tscientific.co.kr'),
 (1,'WEB','infraeye1 - web','10.110.1.104',60000,'https://10.110.1.104:60000','m_user','wByhfewFrYAgEXpTejpd5ZXGJ13zq+bh+c44IupMvuM=',20022,'root','+5j99UFl89RKCXLI7umAH2Vh4BnPLBW+FdArBBST2aM=','infraeye 1 개발',1,1,'admin@tscientific.co.kr'),
 (2,'WEB','infraeye2 - web','10.110.1.103',13306,'http://10.110.1.103','m_user','0HbpMLwxX6SDVmTrvFTB7pso4w403s3fZQRdQloZ8Vo=',20022,'root','BHCx/hLfW4uoSiaGqgMtb4QegWd0ZWgN2XVmE/Bo4ws=','infraeye2 개발 서버',1,1,'admin@tscientific.co.kr'),
 (2,'DATABASE','infraeye2 - mariadb','10.110.1.103',13306,NULL,'infraeye','87QvTx21kwVeBWtsku5TNvkttDm3JonQYkjlHO0Klu4=',NULL,NULL,NULL,'infraeye 2 개발서버 MariaDB',2,1,'admin@tscientific.co.kr'),
 (1,'DATABASE','infraeye1 - mariadb','10.110.1.104',3306,NULL,'infraeye','QzLtMAyM1yRUVTdlCLHKv7fylOy/h6IDwvRQTrtrY7k=',NULL,NULL,NULL,'infraeye 1 개발 서버 MariaDB',2,1,'admin@tscientific.co.kr'),
 (3,'WEB','infraeye 2 (test) - web','10.140.1.21',60000,'http://10.140.1.21:60000/','m_user','8dbhhxMIN3oqn3ZCMJm13EIvu0ONoARUzxmi+xncn9s=',22,'root','WguoSHHQFVNd8n3dDUgk43oxbYjF5A6GxqZH/NDQf1o=','infraeye 2 Test서버 - Web',1,1,'admin@tscientific.co.kr'),
 (3,'DATABASE','infraeye2 (test) - mariadb','10.140.1.21',13306,NULL,'infraeye','BcGHIhwqkFThHAfnEgIq5yRIsaW0f23VIj5WxLLBg4Q=',NULL,NULL,NULL,'infraeye2 Test서버 - MariaDB',2,1,'admin@tscientific.co.kr'),
 (3,'DATABASE','infraeye2 - cratedb','10.140.1.21',15432,NULL,'infraeye','m8yxGP/rPFgb1egZqo/c8AmHkGx7nf4zufA9TdA8xd0=',NULL,NULL,NULL,'infraeye2 Test서버 - cratedb',3,1,'admin@tscientific.co.kr'),
 (3,'DATABASE','infraeye2 (test) - redis','10.230.1.17',55501,NULL,NULL,'cl96nziMiSjC7nZo5Srdp8SWY9YuUiDpjs2qlMtumq0=',NULL,NULL,NULL,'infraeye 2 Test서버 - redis',4,1,'admin@tscientific.co.kr');
