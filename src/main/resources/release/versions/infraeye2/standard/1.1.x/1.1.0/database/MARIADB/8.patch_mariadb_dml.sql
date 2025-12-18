/*
 * 대상 설치본 버전: InfraEye-2.0.0.241127-STD.tar.gz,  infra2_img_2.0.0_250204.tar.gz
 * 주요 내용: SMS 추가에 따른 DML 작업
 * */

DELIMITER //
DROP PROCEDURE IF EXISTS NMS_DB.p_patch_dml;
CREATE PROCEDURE p_patch_dml()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT '오류 발생: 모든 DML 작업을 롤백했습니다.' AS RESULT;
    END;
    START TRANSACTION;
    /*****************************   패치 내용 시작   *****************************/
    -- 서버#Agent 장비 구분 추가
    INSERT INTO NMS_DB.MCH_GB_INFO (MCH_GB_CD,MCH_GB_NM,MCH_GB_NM_EN,VIEW_ORDER,OLD_CD,IMG_CD,VIEW_YN) VALUES
    (14, '서버#Agent', 'Server#Agent', 12, '8_SVR', 'NM00000125', 'Y');

    -- 기존 서버 장비구분을 서버#Snmp로 변경
    UPDATE NMS_DB.MCH_GB_INFO
    SET MCH_GB_NM = '서버#Snmp', MCH_GB_NM_EN = 'Server#Snmp'
    WHERE MCH_GB_CD = 5;

    -- 서버#Snmp의 장비 모델 추가
    INSERT INTO NMS_DB.MCH_MODEL_INFO (
        MCH_TYPE_CD,
        VENDOR_CD,
        MODEL_NM,
        SYS_OBJECTID,
        OID_CPU,
        OID_MEMORY,
        OID_SYS_UPTIME,
        OID_SERIALNO,
        OID_TEMP,
        OID_IF_ALIAS,
        MEMORY_CALC,
        MEMORY_RULE,
        TOTAL_RULE,
        CPU_RULE,
        TEMP_RULE,
        DEFAULT_YN,
        PVT_MIB_YN,
        REG_DT,
        UPD_DT
    ) VALUES
    ('SVR_01', 'CENTOS', 'CentOS', '', '', '', '', '', '', '', 1024, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'HP', 'HP-UX', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'MICROSOFT', 'DomainController', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'MICROSOFT', 'Server', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'MICROSOFT', 'Workstation', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'FORE_SCOUT', 'NAC', '', '', '', '', '', '', '', 1024, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'CISCO', 'ACS Server', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'HP', 'HP Aurba 6000', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'VMWARE', 'vmwEVCA', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'VMWARE', 'vmwESX', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'CISCO', 'UCSC-C240-M4', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'CISCO', 'TelePresence Video Communication Server', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'CISCO', 'UCSC-C240-M5', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'RIVERBED', 'SteelCentral AppResponse11', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'CISCO', 'WSA S195', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'INFOBLOX', 'IB-1415', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'AXGATE', 'AXGATE-TMS-1000', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'SUN', 'htsvt97', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'FUJITSU', 'DX90S2', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'FUJITSU', 'DX600S3', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'FUJITSU', 'E4K300', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'FUJITSU', 'DX410', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36'),
    ('SVR_01', 'ASUS', 'PRIME B760M-A', '', '', '', '', '', '', '', 1, '', '', '', '', 'Y', 'N', '2025-08-26 01:20:36', '2025-08-26 01:20:36');

    -- 서버#Agent의 장비 타입 추가
    INSERT INTO NMS_DB.MCH_TYPE_INFO (MCH_TYPE_CD, POLICY_ID, MCH_GB_CD, MCH_TYPE_NM, MCH_TYPE_NM_EN, IMG_CD, DEVICE_TYPE_CD) VALUES
    ('SVR_01', 3, 14, '서버', 'SERVER', 'NM00000032', 0);

    -- SMS 에이전트 파일
    INSERT INTO NMS_DB.SMS_AGENT_FILE (FILE_NAME,FILE_DESCRIPTION,OS,AGENT_VERSION,FILE_SIZE,FILE_PATH) VALUES
    ('agent_1.0.0.zip','Default Linux Agent 1.0.0','linux','1.0.0',42676171,'/opt/infraeye/data/sms_agent/files/linux/agent_1.0.0-linux.zip'),
    ('agent_1.0.0_windows_x64.zip','Default windows Agent 1.0.0','windows','1.0.0',27101123,'/opt/infraeye/data/sms_agent/files/windows/agent_1.0.0_windows_x64.zip'),
    ('agent_1.0.0-aix.zip','Default aix Agent 1.0.0','aix','1.0.0',42676171,'/opt/infraeye/data/sms_agent/files/aix/agent_1.0.0-aix.zip');

    -- SMS 지표
    INSERT INTO NMS_DB.SMS_INDICATOR( INDICATOR_TYPE, INDICATOR_CD, INDICATOR_NAME, INDICATOR_UNIT, INDICATOR_DATA_TYPE, EVENT_TYPE, EVENT_CD, EVENT_LEVEL, EVENT_MSG_FMT ) VALUES ('cpu', 'SMS_CPU_USED_PERCENT', 'CPU 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3100'), '3100', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_CPU_WAIT_PERCENT', 'CPU 대기율', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3101'), '3101', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_CPU_IDLE_PERCENT', 'CPU 유휴율', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3102'), '3102', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_CPU_USER_PERCENT', 'CPU 사용자 모드 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3103'), '3103', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_CPU_SYSTEM_PERCENT', 'CPU 시스템 모드 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3104'), '3104', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_CPU_NICE_PERCENT', 'CPU Nice 프로세스 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3105'), '3105', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_LOAD_AVG_1MIN', '1분 평균 로드', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3106'), '3106', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_LOAD_AVG_5MIN', '5분 평균 로드', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3107'), '3107', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('cpu', 'SMS_LOAD_AVG_15MIN', '15분 평균 로드', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3108'), '3108', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_USED_PERCENT', '메모리 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3200'), '3200', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_FREE_PERCENT', '메모리 여유율', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3201'), '3201', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_CACHED_PERCENT', '캐시된 메모리 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3202'), '3202', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_BUFFERS_PERCENT', '버퍼 메모리 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3203'), '3203', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_FREE_BYTES', '여유 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3204'), '3204', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_USED_BYTES', '사용 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3205'), '3205', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_CACHED_BYTES', '캐시 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3206'), '3206', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_MEM_BUFFERS_BYTES', '버퍼 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3207'), '3207', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_SWAP_FREE_BYTES', '여유 스왑 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3208'), '3208', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_SWAP_USED_BYTES', '사용 스왑 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3209'), '3209', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_SWAP_USED_PERCENT', '스왑 메모리 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3210'), '3210', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_SWAP_CACHED_BYTES', '캐시된 스왑 메모리 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3211'), '3211', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('mem', 'SMS_SWAP_CACHED_PERCENT', '캐시된 스왑 메모리 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3212'), '3212', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_TOTAL_DISK_USED_PERCENT', '전체 디스크 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3300'), '3300', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_READ_BYTES', '디스크 읽기 바이트', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3301'), '3301', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_WRITE_BYTES', '디스크 쓰기 바이트', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3302'), '3302', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_READ_COUNT', '디스크 읽기 횟수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3303'), '3303', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_WRITE_COUNT', '디스크 쓰기 횟수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3304'), '3304', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_BUSY_RATE_PERCENT', '디스크 활용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3305'), '3305', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_USED_BYTES', '디스크 사용 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3306'), '3306', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_FREE_BYTES', '디스크 여유 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3307'), '3307', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_DISK_USED_PERCENT', '디스크 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3308'), '3308', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_PARTITION_USED_BYTES', '파티션 사용 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3309'), '3309', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_PARTITION_FREE_BYTES', '파티션 여유 용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3310'), '3310', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('disk', 'SMS_PARTITION_USED_PERCENT', '파티션 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3311'), '3311', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('network', 'SMS_NIC_IN_BPS', '네트워크 수신 속도', 'bps', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3400'), '3400', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('network', 'SMS_NIC_OUT_BPS', '네트워크 송신 속도', 'bps', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3401'), '3401', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('network', 'SMS_NIC_IN_PPS', '네트워크 수신 패킷 속도', 'pps', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3402'), '3402', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('network', 'SMS_NIC_OUT_PPS', '네트워크 송신 패킷 속도', 'pps', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3403'), '3403', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('network', 'SMS_NIC_IN_ERR_COUNT', '네트워크 수신 오류 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3404'), '3404', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('network', 'SMS_NIC_OUT_ERR_COUNT', '네트워크 송신 오류 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3405'), '3405', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_PROCESS_COUNT', '프로세스 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3500'), '3500', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_PROCESS_CPU_PERCENT', '프로세스 CPU 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3501'), '3501', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_PROCESS_MEM_PERCENT', '프로세스 메모리 사용률', 'percent', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3502'), '3502', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_PROCESS_MEM_BYTES', '프로세스 메모리 사용량', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3503'), '3503', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_THREAD_COUNT', '스레드 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3504'), '3504', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_PROCESS_RUN_TIME', '프로세스 실행 시간', 'second', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3505'), '3505', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('process', 'SMS_PROCESS_IO_BYTES', '프로세스 IO 바이트', 'bytes', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3506'), '3506', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_SESSION_COUNT', '전체 세션 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3600'), '3600', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_LISTEN_COUNT', 'Listen 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3601'), '3601', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_SYN_SENT_COUNT', 'SYN Sent 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3602'), '3602', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_SYN_RECV_COUNT', 'SYN Recv 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3603'), '3603', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_ESTABLISHED_COUNT', 'Established 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3604'), '3604', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_FIN_WAIT1_COUNT', 'Fin Wait1 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3605'), '3605', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_FIN_WAIT2_COUNT', 'Fin Wait2 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3606'), '3606', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_CLOSE_WAIT_COUNT', 'Close Wait 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3607'), '3607', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_CLOSING_COUNT', 'Closing 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3608'), '3608', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_CLOSED_COUNT', 'Closed 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3609'), '3609', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_LAST_ACK_COUNT', 'Last Ack 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3610'), '3610', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_TIME_WAIT_COUNT', 'Time Wait 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3611'), '3611', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]')
    , ('service', 'SMS_UNKNOWN_COUNT', 'Unknown 상태 연결 수', 'count', '2', (SELECT TYPE_ID FROM EVENT_CD_INFO WHERE CD_ID='3612'), '3612', 2, '$1 $3이벤트:현재[$A$4] 기준[$B$4][$2]');

    -- SMS_AGENT_POLICY_TMPL 테이블 기본 데이터 1개 추가
    INSERT INTO NMS_DB.SMS_AGENT_POLICY_TMPL (
        TMPL_NAME,              -- 입력 필요 (UNIQUE)
        TMPL_DESCRIPTION,       -- NULL 허용
        IS_ENABLED,             -- DEFAULT 'Y'
        IS_DEFAULT,             -- DEFAULT 'N'
        CREATED_BY,             -- 입력 필요
        CREATED_AT,             -- DEFAULT CURRENT_TIMESTAMP
        UPDATED_BY,             -- 입력 필요
        UPDATED_AT              -- DEFAULT CURRENT_TIMESTAMP
    ) VALUES
    ('기본 에이전트 정책 템플릿', '장비 등록 시 사용할 기본 에이전트 템플릿', DEFAULT, 'Y', 'default', DEFAULT, 'default', DEFAULT);

    -- SMS_AGENT_POLICY_TMPL_INDICATOR 테이블 기본 데이터 6개 추가 (각 지표 타입별)
    INSERT INTO NMS_DB.SMS_AGENT_POLICY_TMPL_INDICATOR (
        AGENT_POLICY_TMPL_ID,   -- 외래키 (위에서 생성된 템플릿 ID)
        INDICATOR_TYPE,         -- 입력 필요
        IS_COLLECT,             -- DEFAULT 'Y'
        COLLECT_INTERVAL,       -- DEFAULT 300
        CREATED_BY,             -- 입력 필요
        CREATED_AT,             -- DEFAULT CURRENT_TIMESTAMP
        UPDATED_BY,             -- 입력 필요
        UPDATED_AT              -- DEFAULT CURRENT_TIMESTAMP
    ) VALUES
    (1, 'cpu', DEFAULT, DEFAULT, 'default', DEFAULT, 'default', DEFAULT),
    (1, 'disk', DEFAULT, DEFAULT, 'default', DEFAULT, 'default', DEFAULT),
    (1, 'mem', DEFAULT, DEFAULT, 'default', DEFAULT, 'default', DEFAULT),
    (1, 'network', DEFAULT, DEFAULT, 'default', DEFAULT, 'default', DEFAULT),
    (1, 'process', DEFAULT, DEFAULT, 'default', DEFAULT, 'default', DEFAULT),
    (1, 'service', DEFAULT, DEFAULT, 'default', DEFAULT, 'default', DEFAULT);

    -- SMS_EVENT_POLICY_TMPL 테이블 기본 데이터 1개 추가
    INSERT INTO NMS_DB.SMS_EVENT_POLICY_TMPL (
        TMPL_NAME,              -- 입력 필요 (UNIQUE)
        TMPL_DESCRIPTION,       -- NULL 허용
        IS_ENABLED,             -- DEFAULT 'Y'
        IS_DEFAULT,             -- DEFAULT 'N'
        CREATED_BY,             -- 입력 필요
        UPDATED_BY              -- 입력 필요
    ) VALUES
    ('기본 이벤트 정책 템플릿', '장비 등록 시 사용 할 기본 이벤트 정책 템플릿', 'N', 'Y', 'default', 'default');

    -- SMS_EVENT_POLICY_THRESHOLD 테이블 지표별 기본 데이터 55개 추가
    INSERT INTO NMS_DB.SMS_EVENT_POLICY_THRESHOLD (
        THRESHOLD_DESCRIPTION, -- 임계치 설명
        IS_ENABLED,           -- DEFAULT 'Y'
        INDICATOR_ID          -- 지표 ID
    ) VALUES
    -- CPU 지표 임계치 (9개)
    ('CPU 사용률에 대한 기본 임계치', DEFAULT, 1),
    ('CPU 대기율에 대한 기본 임계치', DEFAULT, 2),
    ('CPU 유휴율에 대한 기본 임계치 (낮을수록 위험)', DEFAULT, 3),
    ('CPU 사용자 사용률에 대한 기본 임계치', DEFAULT, 4),
    ('CPU 시스템 사용률에 대한 기본 임계치', DEFAULT, 5),
    ('CPU Nice 사용률에 대한 기본 임계치', DEFAULT, 6),
    ('평균 부하 (1분)에 대한 기본 임계치', DEFAULT, 7),
    ('평균 부하 (5분)에 대한 기본 임계치', DEFAULT, 8),
    ('평균 부하 (15분)에 대한 기본 임계치', DEFAULT, 9),

    -- MEMORY 지표 임계치 (13개)
    ('메모리 사용률에 대한 기본 임계치', DEFAULT, 10),
    ('메모리 여유율에 대한 기본 임계치 (낮을수록 위험)', DEFAULT, 11),
    ('메모리 캐시 사용률에 대한 기본 임계치', DEFAULT, 12),
    ('메모리 버퍼 사용률에 대한 기본 임계치', DEFAULT, 13),
    ('여유 메모리 용량에 대한 기본 임계치 (낮을수록 위험)', DEFAULT, 14),
    ('사용 메모리 용량에 대한 기본 임계치', DEFAULT, 15),
    ('캐시 메모리 용량에 대한 기본 임계치', DEFAULT, 16),
    ('버퍼 메모리 용량에 대한 기본 임계치', DEFAULT, 17),
    ('여유 스왑 용량에 대한 기본 임계치 (낮을수록 위험)', DEFAULT, 18),
    ('사용 스왑 용량에 대한 기본 임계치', DEFAULT, 19),
    ('스왑 사용률에 대한 기본 임계치', DEFAULT, 20),
    ('스왑 캐시 용량에 대한 기본 임계치', DEFAULT, 21),
    ('스왑 캐시 사용률에 대한 기본 임계치', DEFAULT, 22),

    -- DISK 지표 임계치 (12개)
    ('전체 디스크 사용률에 대한 기본 임계치', DEFAULT, 23),
    ('디스크 읽기 바이트에 대한 기본 임계치', DEFAULT, 24),
    ('디스크 쓰기 바이트에 대한 기본 임계치', DEFAULT, 25),
    ('디스크 읽기 횟수에 대한 기본 임계치', DEFAULT, 26),
    ('디스크 쓰기 횟수에 대한 기본 임계치', DEFAULT, 27),
    ('디스크 활용율에 대한 기본 임계치', DEFAULT, 28),
    ('디스크 사용 용량에 대한 기본 임계치', DEFAULT, 29),
    ('디스크 여유 용량에 대한 기본 임계치 (낮을수록 위험)', DEFAULT, 30),
    ('디스크 사용률에 대한 기본 임계치', DEFAULT, 31),
    ('파티션 사용 용량에 대한 기본 임계치', DEFAULT, 32),
    ('파티션 여유 용량에 대한 기본 임계치 (낮을수록 위험)', DEFAULT, 33),
    ('파티션 사용률에 대한 기본 임계치', DEFAULT, 34),

    -- NETWORK 지표 임계치 (6개)
    ('네트워크 수신 속도에 대한 기본 임계치', DEFAULT, 35),
    ('네트워크 송신 속도에 대한 기본 임계치', DEFAULT, 36),
    ('네트워크 수신 패킷에 대한 기본 임계치', DEFAULT, 37),
    ('네트워크 송신 패킷에 대한 기본 임계치', DEFAULT, 38),
    ('네트워크 수신 오류에 대한 기본 임계치', DEFAULT, 39),
    ('네트워크 송신 오류에 대한 기본 임계치', DEFAULT, 40),

    -- PROCESS 지표 임계치 (2개)
    ('프로세스 수에 대한 기본 임계치', DEFAULT, 41),
    ('스레드 수에 대한 기본 임계치', DEFAULT, 45),

    -- SERVICE 지표 임계치 (13개)
    ('세션 수에 대한 기본 임계치', DEFAULT, 48),
    ('Listen 상태 연결 수에 대한 기본 임계치', DEFAULT, 49),
    ('SYN Sent 상태 연결 수에 대한 기본 임계치', DEFAULT, 50),
    ('SYN Recv 상태 연결 수에 대한 기본 임계치', DEFAULT, 51),
    ('Established 상태 연결 수에 대한 기본 임계치', DEFAULT, 52),
    ('Fin Wait1 상태 연결 수에 대한 기본 임계치', DEFAULT, 53),
    ('Fin Wait2 상태 연결 수에 대한 기본 임계치', DEFAULT, 54),
    ('Close Wait 상태 연결 수에 대한 기본 임계치', DEFAULT, 55),
    ('Closing 상태 연결 수에 대한 기본 임계치', DEFAULT, 56),
    ('Closed 상태 연결 수에 대한 기본 임계치', DEFAULT, 57),
    ('Last Ack 상태 연결 수에 대한 기본 임계치', DEFAULT, 58),
    ('Time Wait 상태 연결 수에 대한 기본 임계치', DEFAULT, 59),
    ('Unknown 상태 연결 수에 대한 기본 임계치', DEFAULT, 60);

    -- SMS_EVENT_POLICY_THRESHOLD_CONDITION 테이블 기본 데이터 추가 (임계치 55개 * 이벤트등급 6등급 = 330개)
    INSERT INTO NMS_DB.SMS_EVENT_POLICY_THRESHOLD_CONDITION (
        THRESHOLD_ID,             -- 임계치 ID
        CONDITION_EVENT_LEVEL,    -- 0:정상, 1:긴급, 2:높음, 3:낮음, 4:경고, 5:정보
        CONDITION_VALUE,          -- 임계치 조건 값
        CONDITION_OPERATOR        -- DEFAULT 'O'
    ) VALUES
    -- CPU 사용률 임계치 조건
    (1, 0, 30.00, DEFAULT),
    (1, 1, 95.00, DEFAULT),
    (1, 2, 85.00, DEFAULT),
    (1, 3, 75.00, DEFAULT),
    (1, 4, 65.00, DEFAULT),
    (1, 5, 50.00, DEFAULT),

    -- CPU 대기율 임계치 조건
    (2, 0, 40.00, DEFAULT),
    (2, 1, 95.00, DEFAULT),
    (2, 2, 90.00, DEFAULT),
    (2, 3, 80.00, DEFAULT),
    (2, 4, 70.00, DEFAULT),
    (2, 5, 60.00, DEFAULT),

    -- CPU 유휴율 임계치 조건 - 낮을수록 위험
    (3, 0, 50.00, 'U'),
    (3, 1, 5.00, 'U'),
    (3, 2, 10.00, 'U'),
    (3, 3, 15.00, 'U'),
    (3, 4, 20.00, 'U'),
    (3, 5, 30.00, 'U'),

    -- CPU 사용자 사용률 임계치 조건
    (4, 0, 25.00, DEFAULT),
    (4, 1, 85.00, DEFAULT),
    (4, 2, 75.00, DEFAULT),
    (4, 3, 65.00, DEFAULT),
    (4, 4, 55.00, DEFAULT),
    (4, 5, 45.00, DEFAULT),

    -- CPU 시스템 사용률 임계치 조건
    (5, 0, 15.00, DEFAULT),
    (5, 1, 70.00, DEFAULT),
    (5, 2, 60.00, DEFAULT),
    (5, 3, 50.00, DEFAULT),
    (5, 4, 40.00, DEFAULT),
    (5, 5, 30.00, DEFAULT),

    -- CPU Nice 사용률 임계치 조건
    (6, 0, 5.00, DEFAULT),
    (6, 1, 40.00, DEFAULT),
    (6, 2, 30.00, DEFAULT),
    (6, 3, 25.00, DEFAULT),
    (6, 4, 20.00, DEFAULT),
    (6, 5, 15.00, DEFAULT),

    -- 평균 부하 (1분) 임계치 조건
    (7, 0, 1.50, DEFAULT),
    (7, 1, 8.00, DEFAULT),
    (7, 2, 6.00, DEFAULT),
    (7, 3, 5.00, DEFAULT),
    (7, 4, 4.00, DEFAULT),
    (7, 5, 3.00, DEFAULT),

    -- 평균 부하 (5분) 임계치 조건
    (8, 0, 1.25, DEFAULT),
    (8, 1, 7.00, DEFAULT),
    (8, 2, 5.50, DEFAULT),
    (8, 3, 4.50, DEFAULT),
    (8, 4, 3.50, DEFAULT),
    (8, 5, 2.50, DEFAULT),

    -- 평균 부하 (15분) 임계치 조건
    (9, 0, 1.00, DEFAULT),
    (9, 1, 6.00, DEFAULT),
    (9, 2, 5.00, DEFAULT),
    (9, 3, 4.00, DEFAULT),
    (9, 4, 3.00, DEFAULT),
    (9, 5, 2.00, DEFAULT),

    -- 메모리 사용률 임계치 조건
    (10, 0, 50.00, DEFAULT),
    (10, 1, 95.00, DEFAULT),
    (10, 2, 90.00, DEFAULT),
    (10, 3, 85.00, DEFAULT),
    (10, 4, 80.00, DEFAULT),
    (10, 5, 70.00, DEFAULT),

    -- 메모리 여유율 임계치 조건 - 낮을수록 위험
    (11, 0, 50.00, 'U'),
    (11, 1, 5.00, 'U'),
    (11, 2, 10.00, 'U'),
    (11, 3, 15.00, 'U'),
    (11, 4, 20.00, 'U'),
    (11, 5, 30.00, 'U'),

    -- 메모리 캐시 사용률 임계치 조건
    (12, 0, 15.00, DEFAULT),
    (12, 1, 50.00, DEFAULT),
    (12, 2, 40.00, DEFAULT),
    (12, 3, 35.00, DEFAULT),
    (12, 4, 30.00, DEFAULT),
    (12, 5, 25.00, DEFAULT),

    -- 메모리 버퍼 사용률 임계치 조건
    (13, 0, 8.00, DEFAULT),
    (13, 1, 40.00, DEFAULT),
    (13, 2, 30.00, DEFAULT),
    (13, 3, 25.00, DEFAULT),
    (13, 4, 20.00, DEFAULT),
    (13, 5, 15.00, DEFAULT),

    -- 여유 메모리 용량 임계치 조건 - 낮을수록 위험
    (14, 0, 6442450944.00, 'U'),
    (14, 1, 1073741824.00, 'U'),
    (14, 2, 1610612736.00, 'U'),
    (14, 3, 2147483648.00, 'U'),
    (14, 4, 3221225472.00, 'U'),
    (14, 5, 4294967296.00, 'U'),

    -- 사용 메모리 용량 임계치 조건
    (15, 0, 5368709120.00, DEFAULT),
    (15, 1, 20401094656.00, DEFAULT),
    (15, 2, 17179869184.00, DEFAULT),
    (15, 3, 15032385536.00, DEFAULT),
    (15, 4, 13631488000.00, DEFAULT),
    (15, 5, 10737418240.00, DEFAULT),

    -- 캐시 메모리 용량 임계치 조건
    (16, 0, 536870912.00, DEFAULT),
    (16, 1, 2147483648.00, DEFAULT),
    (16, 2, 1610612736.00, DEFAULT),
    (16, 3, 1342177280.00, DEFAULT),
    (16, 4, 1073741824.00, DEFAULT),
    (16, 5, 805306368.00, DEFAULT),

    -- 버퍼 메모리 용량 임계치 조건
    (17, 0, 268435456.00, DEFAULT),
    (17, 1, 1073741824.00, DEFAULT),
    (17, 2, 805306368.00, DEFAULT),
    (17, 3, 671088640.00, DEFAULT),
    (17, 4, 536870912.00, DEFAULT),
    (17, 5, 402653184.00, DEFAULT),

    -- 여유 스왑 용량 임계치 조건 - 낮을수록 위험
    (18, 0, 8589934592.00, 'U'),
    (18, 1, 2147483648.00, 'U'),
    (18, 2, 3221225472.00, 'U'),
    (18, 3, 4294967296.00, 'U'),
    (18, 4, 5368709120.00, 'U'),
    (18, 5, 6442450944.00, 'U'),

    -- 사용 스왑 용량 임계치 조건
    (19, 0, 1610612736.00, DEFAULT),
    (19, 1, 8589934592.00, DEFAULT),
    (19, 2, 6442450944.00, DEFAULT),
    (19, 3, 5368709120.00, DEFAULT),
    (19, 4, 4294967296.00, DEFAULT),
    (19, 5, 3221225472.00, DEFAULT),

    -- 스왑 사용률 임계치 조건
    (20, 0, 20.00, DEFAULT),
    (20, 1, 80.00, DEFAULT),
    (20, 2, 70.00, DEFAULT),
    (20, 3, 60.00, DEFAULT),
    (20, 4, 50.00, DEFAULT),
    (20, 5, 40.00, DEFAULT),

    -- 스왑 캐시 용량 임계치 조건
    (21, 0, 536870912.00, DEFAULT),
    (21, 1, 2147483648.00, DEFAULT),
    (21, 2, 1610612736.00, DEFAULT),
    (21, 3, 1342177280.00, DEFAULT),
    (21, 4, 1073741824.00, DEFAULT),
    (21, 5, 805306368.00, DEFAULT),

    -- 스왑 캐시 사용률 임계치 조건
    (22, 0, 15.00, DEFAULT),
    (22, 1, 50.00, DEFAULT),
    (22, 2, 40.00, DEFAULT),
    (22, 3, 35.00, DEFAULT),
    (22, 4, 30.00, DEFAULT),
    (22, 5, 25.00, DEFAULT),

    -- 전체 디스크 사용률 임계치 조건
    (23, 0, 50.00, DEFAULT),
    (23, 1, 95.00, DEFAULT),
    (23, 2, 90.00, DEFAULT),
    (23, 3, 85.00, DEFAULT),
    (23, 4, 80.00, DEFAULT),
    (23, 5, 70.00, DEFAULT),

    -- 디스크 읽기 바이트 임계치 조건
    (24, 0, 268435456.00, DEFAULT),
    (24, 1, 5368709120.00, DEFAULT),
    (24, 2, 3221225472.00, DEFAULT),
    (24, 3, 2147483648.00, DEFAULT),
    (24, 4, 1073741824.00, DEFAULT),
    (24, 5, 536870912.00, DEFAULT),

    -- 디스크 쓰기 바이트 임계치 조건
    (25, 0, 268435456.00, DEFAULT),
    (25, 1, 5368709120.00, DEFAULT),
    (25, 2, 3221225472.00, DEFAULT),
    (25, 3, 2147483648.00, DEFAULT),
    (25, 4, 1073741824.00, DEFAULT),
    (25, 5, 536870912.00, DEFAULT),

    -- 디스크 읽기 횟수 임계치 조건
    (26, 0, 2500.00, DEFAULT),
    (26, 1, 50000.00, DEFAULT),
    (26, 2, 30000.00, DEFAULT),
    (26, 3, 20000.00, DEFAULT),
    (26, 4, 10000.00, DEFAULT),
    (26, 5, 5000.00, DEFAULT),

    -- 디스크 쓰기 횟수 임계치 조건
    (27, 0, 2500.00, DEFAULT),
    (27, 1, 50000.00, DEFAULT),
    (27, 2, 30000.00, DEFAULT),
    (27, 3, 20000.00, DEFAULT),
    (27, 4, 10000.00, DEFAULT),
    (27, 5, 5000.00, DEFAULT),

    -- 디스크 활용률 임계치 조건
    (28, 0, 50.00, DEFAULT),
    (28, 1, 95.00, DEFAULT),
    (28, 2, 90.00, DEFAULT),
    (28, 3, 85.00, DEFAULT),
    (28, 4, 80.00, DEFAULT),
    (28, 5, 70.00, DEFAULT),

    -- 디스크 사용 용량 임계치 조건
    (29, 0, 549755813888.00, DEFAULT),
    (29, 1, 2089072394240.00, DEFAULT),
    (29, 2, 1979121302528.00, DEFAULT),
    (29, 3, 1869170210816.00, DEFAULT),
    (29, 4, 879609302220.80, DEFAULT),
    (29, 5, 769558175744.00, DEFAULT),

    -- 디스크 여유 용량 임계치 조건 - 낮을수록 위험
    (30, 0, 659706977689.60, 'U'),
    (30, 1, 109951162777.60, 'U'),
    (30, 2, 164926744166.40, 'U'),
    (30, 3, 219902325555.20, 'U'),
    (30, 4, 329853488332.80, 'U'),
    (30, 5, 439804651110.40, 'U'),

    -- 디스크 사용률 임계치 조건
    (31, 0, 50.00, DEFAULT),
    (31, 1, 95.00, DEFAULT),
    (31, 2, 90.00, DEFAULT),
    (31, 3, 85.00, DEFAULT),
    (31, 4, 80.00, DEFAULT),
    (31, 5, 70.00, DEFAULT),

    -- 파티션 사용 용량 임계치 조건
    (32, 0, 274877906944.00, DEFAULT),
    (32, 1, 1044536197120.00, DEFAULT),
    (32, 2, 989585105408.00, DEFAULT),
    (32, 3, 934634013696.00, DEFAULT),
    (32, 4, 439804651110.40, DEFAULT),
    (32, 5, 384779087872.00, DEFAULT),

    -- 파티션 여유 용량 임계치 조건 - 낮을수록 위험
    (33, 0, 329853488844.80, 'U'),
    (33, 1, 54975581388.80, 'U'),
    (33, 2, 82463372083.20, 'U'),
    (33, 3, 109951162777.60, 'U'),
    (33, 4, 164926744166.40, 'U'),
    (33, 5, 219902325555.20, 'U'),

    -- 파티션 사용률 임계치 조건
    (34, 0, 50.00, DEFAULT),
    (34, 1, 95.00, DEFAULT),
    (34, 2, 90.00, DEFAULT),
    (34, 3, 85.00, DEFAULT),
    (34, 4, 80.00, DEFAULT),
    (34, 5, 70.00, DEFAULT),

    -- 네트워크 수신 속도 임계치 조건
    (35, 0, 250000000.00, DEFAULT),
    (35, 1, 5000000000.00, DEFAULT),
    (35, 2, 3000000000.00, DEFAULT),
    (35, 3, 2000000000.00, DEFAULT),
    (35, 4, 1000000000.00, DEFAULT),
    (35, 5, 500000000.00, DEFAULT),

    -- 네트워크 송신 속도 임계치 조건
    (36, 0, 250000000.00, DEFAULT),
    (36, 1, 5000000000.00, DEFAULT),
    (36, 2, 3000000000.00, DEFAULT),
    (36, 3, 2000000000.00, DEFAULT),
    (36, 4, 1000000000.00, DEFAULT),
    (36, 5, 500000000.00, DEFAULT),

    -- 네트워크 수신 패킷 임계치 조건
    (37, 0, 250000.00, DEFAULT),
    (37, 1, 5000000.00, DEFAULT),
    (37, 2, 3000000.00, DEFAULT),
    (37, 3, 2000000.00, DEFAULT),
    (37, 4, 1000000.00, DEFAULT),
    (37, 5, 500000.00, DEFAULT),

    -- 네트워크 송신 패킷 임계치 조건
    (38, 0, 250000.00, DEFAULT),
    (38, 1, 5000000.00, DEFAULT),
    (38, 2, 3000000.00, DEFAULT),
    (38, 3, 2000000.00, DEFAULT),
    (38, 4, 1000000.00, DEFAULT),
    (38, 5, 500000.00, DEFAULT),

    -- 네트워크 수신 오류 임계치 조건
    (39, 0, 25.00, DEFAULT),
    (39, 1, 500.00, DEFAULT),
    (39, 2, 300.00, DEFAULT),
    (39, 3, 200.00, DEFAULT),
    (39, 4, 100.00, DEFAULT),
    (39, 5, 50.00, DEFAULT),

    -- 네트워크 송신 오류 임계치 조건
    (40, 0, 25.00, DEFAULT),
    (40, 1, 500.00, DEFAULT),
    (40, 2, 300.00, DEFAULT),
    (40, 3, 200.00, DEFAULT),
    (40, 4, 100.00, DEFAULT),
    (40, 5, 50.00, DEFAULT),

    -- 프로세스 수 임계치 조건
    (41, 0, 200.00, DEFAULT),
    (41, 1, 1000.00, DEFAULT),
    (41, 2, 800.00, DEFAULT),
    (41, 3, 650.00, DEFAULT),
    (41, 4, 500.00, DEFAULT),
    (41, 5, 350.00, DEFAULT),

    -- 스레드 수 임계치 조건
    (42, 0, 500.00, DEFAULT),
    (42, 1, 2000.00, DEFAULT),
    (42, 2, 1500.00, DEFAULT),
    (42, 3, 1250.00, DEFAULT),
    (42, 4, 1000.00, DEFAULT),
    (42, 5, 750.00, DEFAULT),

    -- 세션 수 임계치 조건
    (43, 0, 500.00, DEFAULT),
    (43, 1, 2000.00, DEFAULT),
    (43, 2, 1500.00, DEFAULT),
    (43, 3, 1250.00, DEFAULT),
    (43, 4, 1000.00, DEFAULT),
    (43, 5, 750.00, DEFAULT),

    -- Listen 상태 연결 수 임계치 조건
    (44, 0, 50.00, DEFAULT),
    (44, 1, 200.00, DEFAULT),
    (44, 2, 150.00, DEFAULT),
    (44, 3, 125.00, DEFAULT),
    (44, 4, 100.00, DEFAULT),
    (44, 5, 75.00, DEFAULT),

    -- SYN Sent 상태 연결 수 임계치 조건
    (45, 0, 20.00, DEFAULT),
    (45, 1, 100.00, DEFAULT),
    (45, 2, 75.00, DEFAULT),
    (45, 3, 60.00, DEFAULT),
    (45, 4, 50.00, DEFAULT),
    (45, 5, 40.00, DEFAULT),

    -- SYN Recv 상태 연결 수 임계치 조건
    (46, 0, 20.00, DEFAULT),
    (46, 1, 100.00, DEFAULT),
    (46, 2, 75.00, DEFAULT),
    (46, 3, 60.00, DEFAULT),
    (46, 4, 50.00, DEFAULT),
    (46, 5, 40.00, DEFAULT),

    -- Established 상태 연결 수 임계치 조건
    (47, 0, 400.00, DEFAULT),
    (47, 1, 1600.00, DEFAULT),
    (47, 2, 1200.00, DEFAULT),
    (47, 3, 1000.00, DEFAULT),
    (47, 4, 800.00, DEFAULT),
    (47, 5, 600.00, DEFAULT),

    -- Fin Wait1 상태 연결 수 임계치 조건
    (48, 0, 10.00, DEFAULT),
    (48, 1, 40.00, DEFAULT),
    (48, 2, 30.00, DEFAULT),
    (48, 3, 25.00, DEFAULT),
    (48, 4, 20.00, DEFAULT),
    (48, 5, 15.00, DEFAULT),

    -- Fin Wait2 상태 연결 수 임계치 조건
    (49, 0, 10.00, DEFAULT),
    (49, 1, 40.00, DEFAULT),
    (49, 2, 30.00, DEFAULT),
    (49, 3, 25.00, DEFAULT),
    (49, 4, 20.00, DEFAULT),
    (49, 5, 15.00, DEFAULT),

    -- Close Wait 상태 연결 수 임계치 조건
    (50, 0, 15.00, DEFAULT),
    (50, 1, 60.00, DEFAULT),
    (50, 2, 45.00, DEFAULT),
    (50, 3, 37.50, DEFAULT),
    (50, 4, 30.00, DEFAULT),
    (50, 5, 22.50, DEFAULT),

    -- Closing 상태 연결 수 임계치 조건
    (51, 0, 5.00, DEFAULT),
    (51, 1, 20.00, DEFAULT),
    (51, 2, 15.00, DEFAULT),
    (51, 3, 12.50, DEFAULT),
    (51, 4, 10.00, DEFAULT),
    (51, 5, 7.50, DEFAULT),

    -- Closed 상태 연결 수 임계치 조건
    (52, 0, 5.00, DEFAULT),
    (52, 1, 20.00, DEFAULT),
    (52, 2, 15.00, DEFAULT),
    (52, 3, 12.50, DEFAULT),
    (52, 4, 10.00, DEFAULT),
    (52, 5, 7.50, DEFAULT),

    -- Last Ack 상태 연결 수 임계치 조건
    (53, 0, 10.00, DEFAULT),
    (53, 1, 40.00, DEFAULT),
    (53, 2, 30.00, DEFAULT),
    (53, 3, 25.00, DEFAULT),
    (53, 4, 20.00, DEFAULT),
    (53, 5, 15.00, DEFAULT),

    -- Time Wait 상태 연결 수 임계치 조건
    (54, 0, 50.00, DEFAULT),
    (54, 1, 200.00, DEFAULT),
    (54, 2, 150.00, DEFAULT),
    (54, 3, 125.00, DEFAULT),
    (54, 4, 100.00, DEFAULT),
    (54, 5, 75.00, DEFAULT),

    -- Unknown 상태 연결 수 임계치 조건
    (55, 0, 25.00, DEFAULT),
    (55, 1, 100.00, DEFAULT),
    (55, 2, 75.00, DEFAULT),
    (55, 3, 62.50, DEFAULT),
    (55, 4, 50.00, DEFAULT),
    (55, 5, 37.50, DEFAULT);

    -- SMS_EVENT_POLICY_TMPL_MAPPING 테이블 기본 데이터 추가 (55개 매핑)
    INSERT INTO NMS_DB.SMS_EVENT_POLICY_TMPL_MAPPING (
        EVENT_POLICY_TMPL_ID,   -- 외래키 (SMS_EVENT_POLICY_TMPL_ID)
        THRESHOLD_ID            -- 외래키 (SMS_EVENT_POLICY_THRESHOLD__ID)
    ) VALUES
    (1, 1),   (1, 2),   (1, 3),
    (1, 4),   (1, 5),   (1, 6),
    (1, 7),   (1, 8),   (1, 9),
    (1, 10),  (1, 11),  (1, 12),
    (1, 13),  (1, 14),  (1, 15),
    (1, 16),  (1, 17),  (1, 18),
    (1, 19),  (1, 20),  (1, 21),
    (1, 22),  (1, 23),  (1, 24),
    (1, 25),  (1, 26),  (1, 27),
    (1, 28),  (1, 29),  (1, 30),
    (1, 31),  (1, 32),  (1, 33),
    (1, 34),  (1, 35),  (1, 36),
    (1, 37),  (1, 38),  (1, 39),
    (1, 40),  (1, 41),  (1, 42),
    (1, 43),  (1, 44),  (1, 45),
    (1, 46),  (1, 47),  (1, 48),
    (1, 49),  (1, 50),  (1, 51),
    (1, 52),  (1, 53),  (1, 54),
    (1, 55);

    -- 퍼센트 지표 기본 색상 변경 (기존: 0, 21, 41, 61 모두 #39C3E6)
    UPDATE NMS_DB.ITEM_COLOR_SETUP
    SET COLOR_TABLE = CASE
        WHEN ITEM_GB=2 AND ITEM_CD = 0 THEN '#FFFFFF'
        WHEN ITEM_GB=2 AND ITEM_CD = 21 THEN '#39C3E6'
        WHEN ITEM_GB=2 AND ITEM_CD = 41 THEN '#CDE639'
        WHEN ITEM_GB=2 AND ITEM_CD = 61 THEN '#E69439'
        WHEN ITEM_GB=2 AND ITEM_CD = 81 THEN '#F04F4F'
        ELSE COLOR_TABLE
    END
    WHERE (ITEM_GB=2 AND ITEM_CD = 0)
       OR (ITEM_GB=2 AND ITEM_CD = 21)
       OR (ITEM_GB=2 AND ITEM_CD = 41)
       OR (ITEM_GB=2 AND ITEM_CD = 61)
       OR (ITEM_GB=2 AND ITEM_CD = 81);

    -- 성능지표 1시간 집계 스케줄
    INSERT INTO NMS_DB.NMS_WATCHDOG_SCH (SVR_ID, SCH_NM, SCH_EXE, SCH_PATH, SCH_TYPE, SCH_PERIOD, SCH_START_TIME, SCH_MAX_RUN_TIME, SCH_USE_YN, BACKUP_TYPE ) SELECT @SVR_ID,'성능지표 1시간 집계 스케줄','summary_server_metrics.sh',CONCAT('',@ROOT_PATH, '/netcruz/nms/util/summary_server_metrics.sh 1h &'),1,60,'00:58:00',0,'Y', 0 FROM DUAL WHERE NOT EXISTS( SELECT * FROM NMS_DB.NMS_WATCHDOG_SCH WHERE SVR_ID =  @SVR_ID AND SCH_NM = '성능지표 1시간 집계 스케줄');
    -- 성능지표 1day 집계 스케줄
    INSERT INTO NMS_DB.NMS_WATCHDOG_SCH (SVR_ID, SCH_NM, SCH_EXE, SCH_PATH, SCH_TYPE, SCH_PERIOD, SCH_START_TIME, SCH_MAX_RUN_TIME, SCH_USE_YN, BACKUP_TYPE ) SELECT @SVR_ID,'성능지표 1day 집계 스케줄','summary_server_metrics.sh',CONCAT('',@ROOT_PATH, '/netcruz/nms/util/summary_server_metrics.sh 1d &'),2,0,'01:00:00',0,'Y', 0 FROM DUAL WHERE NOT EXISTS( SELECT * FROM NMS_DB.NMS_WATCHDOG_SCH WHERE SVR_ID =  @SVR_ID AND SCH_NM = '성능지표 1day 집계 스케줄');

    -- 관리 > 솔루션관리 > 서버관리 > 서버관리수정 > 엔진목록
    -- NC_SMS_SERVER 항목 추가
    INSERT INTO NMS_DB.NMS_ENGINE_INFO (
        SVR_ID,
        ENGINE_NM,
        ENGINE_EXE,
        ENGINE_PATH,
        STATE_CD,
        START_DELAY,
        RESTART_YN,
        REQ_CMD,
        DEL_YN,
        STATE_LOG_CHK,
        STATE_LOG_PERIOD,
        UPD_DT,
        ST_UPD_DT
    ) VALUES (
        1,
        'NC_SMS_SERVER',
        'NC_SMS_SERVER',
        '/opt/infraeye/nms/bin/NC_SMS_SERVER&',
        'R',
        100,
        'Y',
        0,
        'N',
        'N',
        30,
        NOW(),
        NOW()
    );

    /*****************************   패치 내용 종료   *****************************/
END //
DELIMITER ;

-- 실행 및 패치용 임시 프로시저 삭제
CALL p_patch_dml();
DROP PROCEDURE p_patch_dml;

SELECT 'DML 패치 완료' AS RESULT;
