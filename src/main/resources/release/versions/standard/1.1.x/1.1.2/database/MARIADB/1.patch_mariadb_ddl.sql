drop table if exists NMS_DB.SMS_LOG_MONITOR_POLICY_DETAIL;
create table NMS_DB.SMS_LOG_MONITOR_POLICY_DETAIL
(
    DETAIL_ID      int auto_increment comment '상세 ID'
        primary key,
    POLICY_ID      int                                    not null comment '정책 ID',
    SOURCE_TYPE    varchar(50)                            null comment '소스타입',
    FILE_NAME      varchar(200)                           null comment '로그 파일명',
    FILE_PATH      varchar(500)                           null comment '로그 파일 경로',
    FILE_TYPE      varchar(20)                            null comment '파일 타입 (TEXT/JSON/XML/CSV)',
    FILTER_PATTERN varchar(500)                           null comment '필터 패턴 (정규식)',
    IS_USED        tinyint(1) default 0                   null comment '사용 상태',
    SORT_ORDER     int        default 1                   null comment '정렬 순서',
    DESCRIPTION    varchar(500)                           null comment '상세 설명',
    CREATED_BY     varchar(50)                            not null comment '생성자',
    CREATED_DATE   datetime   default current_timestamp() null comment '생성일시',
    UPDATED_BY     varchar(50)                            null comment '수정자',
    UPDATED_DATE   datetime   default current_timestamp() null on update current_timestamp() comment '수정일시',

    INDEX idx_is_used (IS_USED),
    INDEX idx_policy_id (POLICY_ID),
    INDEX idx_sort_order (SORT_ORDER),
    constraint SMS_LOG_MONITOR_POLICY_DETAIL_ibfk_1
        foreign key (POLICY_ID) references NMS_DB.SMS_LOG_MONITOR_POLICY (POLICY_ID)
            on delete cascade
) comment '로그 모니터링 정책 상세' charset = utf8mb3;