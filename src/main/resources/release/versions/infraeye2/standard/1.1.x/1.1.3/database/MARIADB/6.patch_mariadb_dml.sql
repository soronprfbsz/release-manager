
-- 성능지표 1시간 집계 스케줄
INSERT INTO NMS_DB.NMS_WATCHDOG_SCH (SVR_ID, SCH_NM, SCH_EXE, SCH_PATH, SCH_TYPE, SCH_PERIOD, SCH_START_TIME, SCH_MAX_RUN_TIME, SCH_USE_YN, BACKUP_TYPE ) SELECT @SVR_ID,'성능지표 1시간 집계 스케줄','summary_server_metrics.sh',CONCAT('',@ROOT_PATH, '/netcruz/nms/util/summary_server_metrics.sh 1h &'),1,60,'00:58:00',0,'Y', 0 FROM DUAL WHERE NOT EXISTS( SELECT * FROM NMS_WATCHDOG_SCH WHERE SVR_ID =  @SVR_ID AND SCH_NM = '성능지표 1시간 집계 스케줄');
-- 성능지표 1day 집계 스케줄
INSERT INTO NMS_DB.NMS_WATCHDOG_SCH (SVR_ID, SCH_NM, SCH_EXE, SCH_PATH, SCH_TYPE, SCH_PERIOD, SCH_START_TIME, SCH_MAX_RUN_TIME, SCH_USE_YN, BACKUP_TYPE ) SELECT @SVR_ID,'성능지표 1day 집계 스케줄','summary_server_metrics.sh',CONCAT('',@ROOT_PATH, '/netcruz/nms/util/summary_server_metrics.sh 1d &'),2,0,'01:00:00',0,'Y', 0 FROM DUAL WHERE NOT EXISTS( SELECT * FROM NMS_WATCHDOG_SCH WHERE SVR_ID =  @SVR_ID AND SCH_NM = '성능지표 1day 집계 스케줄');

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

