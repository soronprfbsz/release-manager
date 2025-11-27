/*
 * 
 * 대상 설치본 버전: InfraEye-2.0.0.241127-STD.tar.gz,  infra2_img_2.0.0_250204.tar.gz
 * 패치본 생성일: 2025-10-27
 * 주요 내용: SMS 관련 메뉴 추가
 * 
 * */ 

USE CM_DB;

-- 저장용 임시 테이블 생성
CREATE TEMPORARY TABLE IF NOT EXISTS TMP_MENU_INFO
SELECT * FROM MENU_INFO;

CREATE TEMPORARY TABLE IF NOT EXISTS TMP_ROLE_MENU_PRIV
SELECT * FROM ROLE_MENU_PRIV;

CREATE TEMPORARY TABLE IF NOT EXISTS TMP_ROLE_MENU_BASE
SELECT * FROM ROLE_MENU_PRIV;

CREATE TEMPORARY TABLE IF NOT EXISTS TMP_MENU_MNG
SELECT * FROM MENU_MNG;

TRUNCATE TABLE TMP_MENU_INFO;
TRUNCATE TABLE TMP_ROLE_MENU_PRIV;
TRUNCATE TABLE TMP_ROLE_MENU_BASE;
TRUNCATE TABLE TMP_MENU_MNG;

-- 메뉴코드
INSERT INTO TMP_MENU_INFO (MENU_ID,MENU_NM,MENU_NM_EN,UPPER_MENU_ID,MENU_ORDER,OFFER_YN,MENU_DESC,MENU_TYPE,MENU_LC,SITE_MAP_YN) VALUES('STD050','장비추가','Device Add','',0,'Y','',0,'/common/device/add','Y')
,('STD000','대시보드','Dashboard','',1,'Y','',0,'/dashboard','Y')
,('STD001','운영현황','Operation','',2,'Y','',0,'/operation','Y')
,('STD189','서비스관리','Service','',3,'N','',0,'/service','N')
,('STD127','트래픽분석','Traffic','',4,'N','',0,'/traffic','N')
,('STD184','자산','Assets','',5,'N','',0,'/assets','N')
,('STD002','보고서','Report','',6,'Y','',0,'/report','Y')
,('STD003','관리','Setting','',7,'Y','',0,'/setting','Y')
,('STD031','조직관리','Group','',8,'Y','',0,'/organization','Y')
,('STD004','사이트맵','Site Map','',9,'Y','',0,'siteMap();','Y')
,('STD005','토폴로지 맵','Topology Map','STD000',0,'Y','',0,'#map','Y')
,('STD006','위젯 대시보드','Widget Dashboard','STD000',1,'Y','',0,'#dashboard/widget','Y')
,('STD172','기본대시보드','Default Dashboard','STD000',2,'Y','기본대시보드 1',0,'javascript:basicDashboardPop(1)','Y')
,('STD173','기본대시보드','Default Dashboard','STD000',3,'N','기본대시보드 2',0,'javascript:basicDashboardPop(2)','N')
,('STD174','기본대시보드','Default Dashboard','STD000',4,'N','기본대시보드 3',0,'javascript:basicDashboardPop(3)','N')
,('STD008','AP 위치(카카오)','','STD000',5,'N','',0,'javascript:wirelessMapPop(37.5168,126.8665)','N')
,('STD009','3D 랙실장도','','STD000',6,'N','3D',0,'javascript:rack3DDomDashboardPop()','N')
,('CUS201','경산시청 커스텀대시보드','','STD000',7,'N','경산시청 커스텀대시보드',0,'javascript:commonCustomDashboardPop("gbgs")','N')
,('CSM202','환경부 커스텀대시보드','MEGO Custom Dashboard','STD000',8,'N','환경부 커스텀대시보드',0,'javascript:commonCustomDashboardPop("mego")','N')
,('CSM203','농어촌공사 커스텀대시보드','RURAL Custom Dashboard','STD000',9,'N','농어촌공사 커스텀대시보드',0,'javascript:commonCustomDashboardPop("rural")','N')
,('CSM204','우리투자증권 커스텀대시보드','Woori Custom Dashboard','STD000',10,'N','우리투자증권 커스텀대시보드',0,'javascript:commonCustomDashboardPop("woori")','N')
,('STD014','기본뷰','Default','STD001',0,'Y','',1,'#oper-status/default','Y')
,('STD015','서비스그룹','Group','STD001',1,'Y','',1,'#oper-status/group','Y')
,('STD016','토폴로지','Topology','STD001',2,'Y','',1,'#oper-status/topology','Y')
,('STD128','목록','list','STD001',0,'Y','목록은 MENU_LC가 없기에 btn-list 고정',2,'','Y')
,('STD135','요약','Summary','STD001',1,'Y','운영현황에서 MENU_LC 정의시 유의 사항',2,'/oper-status/common/summary','Y')
,('STD129','장비정보','Device Info','STD001',2,'Y','마지막 /의 URL에 해당하는 값이',2,'/oper-status/common/device','Y')
,('STD147','기본 장비 정보','Basic device info','STD129',0,'Y','각 Tab ID 및 구분자가 됨.',3,'/oper-status/device/basic','Y')
,('STD148','템플릿 정보','Template info','STD129',1,'Y','예) /oper-status/common/device',3,'/oper-status/device/tmpl','Y')
,('STD149','수집 정보','Collect info','STD129',2,'Y','       tab id : btn-device , tab구분 : DEVICE',3,'/oper-status/device/nms_svr','Y')
,('STD150','커스텀 필드','Custom field','STD129',3,'Y','',3,'/oper-status/device/custom','Y')
,('STD151','담당자','Manager','STD129',4,'Y','',3,'/oper-status/device/manager','Y')
,('STD130','인터페이스','Interface','STD001',3,'Y','',2,'/oper-status/common/interface','Y')
,('STD159','인터페이스','Interface','STD130',0,'Y','',3,'/oper-status/interface/interface','Y')
,('STD166','기본 인터페이스 정보','Basic Interface info','STD159',0,'Y','',3,'/oper-status/interface/interfaceInfo','Y')
,('STD167','템플릿 정보','Template info','STD159',1,'Y','',3,'/oper-status/interface/tmpl','Y')
,('STD160','커스텀 필드','Custom field','STD159',2,'Y','',3,'/oper-status/interface/custom','Y')
,('STD161','IP 리스트','IP List','STD130',2,'Y','',3,'/oper-status/interface/iplist','Y')
,('STD162','포트뷰','Port View','STD130',3,'N','',3,'/oper-status/interface/portview','N')
,('STD132','SMS','SMS','STD001',10,'Y','',2,'/oper-status/common/sms','N')
,('STD156','CPU','CPU','STD132',0,'Y','',4,'/oper-status/sms/cpu','N')
,('STD157','파일시스템','File System','STD132',1,'Y','',4,'/oper-status/sms/fileSystem','N')
,('STD158','프로세스','Process','STD132',2,'Y','',4,'/oper-status/sms/process','N')
,('STD131','L4','L4','STD001',11,'Y','',2,'/oper-status/common/l4','N')
,('STD165','Virtual','Virtual','STD131',0,'Y','',4,'/oper-status/l4/virtual','N')
,('STD170','Real','Real','STD131',1,'Y','',4,'/oper-status/l4/real','N')
,('STD133','AP','AP','STD001',12,'Y','',2,'/oper-status/common/ap','N')
,('STD163','AP','AP','STD133',0,'Y','',4,'/oper-status/ap/ap','N')
,('STD164','Client','Client','STD133',1,'Y','',4,'/oper-status/ap/client','N')
,('STD134','UPS','UPS','STD001',13,'Y','',2,'/oper-status/common/ups','N')
,('STD155','UPS','UPS','STD134',0,'Y','',4,'/oper-status/ups/ups','N')
,('STD168','X.25','X.25','STD001',14,'Y','',2,'/oper-status/common/x25','N')
,('STD169','포트','Port','STD168',0,'Y','',4,'/oper-status/x25/port','N')
,('STD171','L/C','L/C','STD168',1,'Y','',4,'/oper-status/x25/lc','N')
,('STD194','VPN','VPN','STD001',15,'Y','',2,'/oper-status/common/vpn','N')
,('STD195','VPN 터널 인터페이스','VPN Tunnel Interface','STD194',0,'Y','',4,'/oper-status/vpn/vpn','N')
,('STD136','운영관리','Operation Management','STD001',100,'Y','',2,'/oper-status/common/oper','Y')
,('STD137','변경이력','Change history','STD136',0,'Y','',4,'/oper-status/oper/chgHistory','Y')
,('STD138','가동률 조회','Operation Ratio','STD136',1,'Y','',4,'/oper-status/oper/operRatio','Y')
,('STD139','컨피그','Config','STD136',2,'Y','',4,'/oper-status/oper/config','Y')
,('STD191','전체 IP 조회','All IP','STD136',3,'Y','',4,'/oper-status/oper/ipAll','Y')
,('STD140','이벤트','Event','STD001',101,'Y','',2,'/oper-status/common/event','Y')
,('STD141','실시간','Real time event','STD140',0,'Y','',4,'/oper-status/event/now','Y')
,('STD142','이벤트 이력','Event history','STD140',1,'Y','',4,'/oper-status/event/history','Y')
,('STD143','Syslog','Syslog','STD140',2,'Y','',4,'/oper-status/event/syslog','Y')
,('STD144','Trap','Trap','STD140',3,'Y','',4,'/oper-status/event/trap','Y')
,('STD145','조치이력','Action history','STD140',4,'Y','',4,'/oper-status/event/acthist','Y')
,('STD146','예외시간','Exception time','STD140',5,'Y','',4,'/oper-status/event/extime','Y')
,('STD190','IP 조회','IP','STD189',0,'Y','',0,'','Y')
,('STD192','VLAN별 IP 조회','IP by VLAN','STD190',1,'N','',0,'#ip/vlan','N')
,('STD193','ARP 정보조회','ARP Information Inquiry','STD190',0,'Y','',0,'#arp/info','Y')
,('STD109','서비스 조회','Service Mgt.','STD189',1,'Y','',0,'','Y')
,('STD110','TCP설정','TCP Service','STD109',1,'Y','',0,'#oper/tcpsvc','Y')
,('STD111','URL체크','URL Service','STD109',2,'Y','',0,'#oper/urlsvc','Y')
,('STD154','TCP조회','TCP','STD109',0,'Y','',0,'#oper/tcp','Y')
,('STD106','IP Node관리','IP Node Mgt.','STD189',2,'N','',0,'','N')
,('STD107','IPAM','IPAM','STD106',0,'N','',0,'#oper/ipam','N')
,('STD108','SPM','SPM','STD106',1,'N','',0,'#oper/spm','N')
,('STD007','운영지원','Operational Support','STD189',4,'N','',0,'','N')
,('STD010','Auto-Discovery','Auto-Discovery','STD007',0,'N','',0,'#oper/autoDiscovery','N')
,('STD011','Root Cause','Root Cause','STD007',1,'N','',0,'#oper/rootCauseWithAdd','N')
,('STD012','구간응답시간','Round-Trip Time','STD007',2,'N','',0,'#oper/rtt','N')
,('STD013','랙실장도','Rack Dashboard','STD007',3,'N','',0,'#oper/rack','N')
,('STD114','TOOL','TOOL','STD189',5,'Y','',0,'','Y')
,('STD115','MIB 브라우저','MIB Browser','STD114',0,'N','기존처럼 팝업으로 할지 고민 필요',0,'javascript:mibDomDashboardPop()','N')
,('STD116','Ping & SNMP 스캔','Ping & SNMP Scan','STD114',1,'Y','',0,'javascript:pingSnmpPop()','Y')
,('STD181','I/F 그룹','I/F Group','STD127',0,'Y','',1,'#traffic-status/interface','Y')
,('STD182','IP 그룹','IP Group','STD127',1,'Y','',1,'#traffic-status/ip','Y')
,('STD183','토폴로지','Topology','STD127',2,'Y','',1,'#traffic-status/topology','N')
,('STD196','장비그룹','Device','STD127',3,'Y','',1,'#traffic-status/device','Y')
,('STD185','장비','Device','STD184',0,'Y','',1,'#assets-status/device','Y')
,('STD186','선번장','Interface','STD184',1,'Y','',1,'#assets-status/interface','Y')
,('STD187','등록 장비','Registration','STD184',2,'Y','',1,'#assets-status/regDevice','Y')
,('STD188','미등록 장비','Unregistration','STD184',3,'Y','',1,'#assets-status/unRegDevice','Y')
,('STD017','성능보고서','Performance Report','STD002',0,'Y','',0,'','Y')
,('STD022','디바이스','Device','STD017',0,'Y','',0,'#rt/device','Y')
,('STD023','인터페이스','Interface','STD017',1,'Y','',0,'#rt/interface','Y')
,('STD024','디바이스(가로정렬)','Device(horizontal)','STD017',2,'N','',0,'#rt/deviceHorizontal','N')
,('STD025','Flow','Flow','STD017',3,'N','',0,'','N')
,('STD028','인터페이스','Interface','STD025',0,'N','',0,'#rt/traffic/interfaceEl','N')
,('STD029','IP Flow그룹','IP Flow Group','STD025',1,'N','',0,'#rt/traffic/ipflowEl','N')
,('STD018','이벤트보고서','Event Report','STD002',1,'Y','',0,'','Y')
,('STD026','이벤트','Event','STD018',0,'Y','',0,'#rt/event','Y')
,('STD019','운영보고서','Operational Report','STD002',2,'Y','',0,'','Y')
,('STD027','시스템','System','STD019',0,'Y','',0,'#rt/system','Y')
,('STD020','스케줄보고서','Schedule Report','STD002',3,'N','',0,'#rt/schedule','N')
,('STD021','커스텀보고서','Custom Report','STD002',4,'N','',0,'','N')
,('STD065','정책관리','Policy Mgt.','STD003',0,'Y','',0,'','Y')
,('STD066','템플릿관리','Template Mgt.','STD065',0,'Y','',0,'','Y')
,('STD067','인증관리','Auth Mgt.','STD066',0,'Y','',0,'#tmpl/auth','Y')
,('STD068','SNMP인증','SNMP Auth.','STD067',0,'Y','',1,'#tmpl/snmp','Y')
,('STD069','접속인증','Terminal Auth.','STD067',1,'Y','',1,'#tmpl/terminalAccount','Y')
,('STD070','장애관리','Fault Mgt','STD066',1,'Y','',0,'#tmpl/fault','Y')
,('STD071','장애감지','Fault Settings','STD070',0,'Y','',1,'#tmpl/faultPolicy','Y')
,('STD072','IP장애감지','IP Fault Settings','STD070',1,'Y','',1,'#tmpl/ipfaultPolicy','Y')
,('STD073','이중화IP관리','Duplication Ip Management','STD070',2,'N','',1,'#dupipmanage','N')
,('STD074','VPN터널장애감지','VPN Tunnel Fault Settings','STD070',3,'N','',1,'#tmpl/vpntunnel','N')
,('STD075','지표관리','Indicator Mgt.','STD066',2,'Y','',0,'#tmpl/indicator','Y')
,('STD076','지표수집','Indicator Collection','STD075',1,'Y','',1,'#tmpl/indicator/limitIndicator/typeA','Y')
,('STD077','지표임계치그룹','Indicator Limitation Group','STD075',0,'Y','',1,'#tmpl/indicator/limitGroup/typeA','Y')
,('STD078','지표설정','Indicator','STD075',2,'Y','',1,'#tmpl/indicator/perfIndicator','Y')
,('STD079','Config관리','Config Mgt.','STD066',3,'Y','',0,'#tmpl/config','Y')
,('STD080','Config백업설정','ConfigBackup Settings','STD079',0,'Y','',1,'#tmpl/confiback','Y')
,('STD081','스크립트설정','Script Settings','STD079',1,'Y','',1,'#tmpl/cliShellCmd','Y')
,('STD082','Config 수집정책','Config Collection Policy','STD079',2,'Y','',1,'#security/event','Y')
,('STD083','Config Push설정','Config Push Settings','STD079',3,'N','',1,'#tmpl/confpush','N')
,('STD084','Password 변경관리','Password Change Mgt.','STD079',4,'N','',1,'#tmpl/password','N')
,('STD085','디바이스설정','Device Settings','STD066',4,'Y','',0,'#tmpl/device','Y')
,('STD086','장비모델설정','Device Model Settings','STD085',0,'Y','',1,'#tmpl/mchModel','Y')
,('STD088','장비종류설정','Device Type Settings','STD085',1,'Y','',1,'#tmpl/mchType','Y')
,('STD087','등록정책','Registration Policy','STD085',2,'Y','',1,'#tmpl/regPolicy','Y')
,('STD089','통보설정','Notification Settings','STD066',5,'Y','',0,'#tmpl/noti_n','Y')
,('STD126','통보설정','Notification Settings','STD089',0,'Y','',1,'#tmpl/noti_n/notiNSetting','Y')
,('STD090','예외시간설정','Exception Settings','STD066',6,'Y','',0,'#tmpl/exTime','Y')
,('STD119','예외시간설정','Exception Settings','STD090',0,'Y','',1,'#tmpl/exTime/exTimeSetting','Y')
,('STD092','Flow정책','Flow Policy','STD065',1,'N','',0,'#flow','N')
,('STD093','애플리케이션 설정','Application Settings','STD092',0,'N','',1,'#flow/app','N')
,('STD094','프로토콜 설정','Protocol Settings','STD092',1,'N','',1,'#flow/protocol','N')
,('STD095','IP 매핑 설정','IP Mapping Settings','STD092',2,'N','',1,'#flow/flowEtc','N')
,('STD096','Rule설정','Rule Settings','STD065',2,'Y','',0,'#rule/ruleset','Y')
,('STD097','Syslog','Syslog Rule','STD096',0,'Y','',1,'#rule/ruleset/syslog','Y')
,('STD098','Trap','Trap Rule','STD096',1,'Y','',1,'#rule/ruleset/trap','Y')
,('STD099','학습임계치데이터설정','Learning Limitation Settings','STD096',2,'N','',1,'#rule/ruleset/learning','N')
,('STD179','AI 설정','AI Settings','STD065',3,'Y','',0,'#rule/aiset','Y')
,('STD180','AI 이상치탐지','AI Outlier Detection','STD179',0,'Y','',1,'#rule/aiset/outlierdetect','Y')
,('STD100','접근제어','Access Control Policy','STD065',6,'Y','',0,'#nac','Y')
,('STD101','사용자 정책','User Policy','STD100',0,'Y','',1,'#nac/userPolicy','Y')
,('STD102','접속IP설정','Remote Access IP Settings','STD100',1,'Y','',1,'#nac/ipSetting','Y')
,('STD152','실시간 현황','Real time status','STD100',2,'Y','',1,'#nac/log/now','Y')
,('STD153','접속 이력','Connection history','STD100',3,'Y','',1,'#nac/log/history','Y')
,('STD030','솔루션관리','Solution Mgt.','STD003',2,'Y','',0,'','Y')
,('STD034','코드관리','Code Mgt.','STD030',0,'Y','',0,'#solution/code','Y')
,('STD051','이벤트코드','Event Code','STD034',0,'Y','',1,'#solution/code/event','Y')
,('STD052','데이터코드','Data Code','STD034',1,'Y','',1,'#solution/code/data','Y')
,('STD035','이미지관리','Image Mgt.','STD030',1,'Y','',0,'#solution/image','Y')
,('STD053','장비아이콘','Device Icon','STD035',0,'Y','',1,'#solution/image/machine','Y')
,('STD054','이미지','Image','STD035',1,'Y','',1,'#solution/image/background','Y')
,('STD055','모델장비이미지','Model IMG','STD035',2,'N','',1,'#solution/image/model','N')
,('STD036','필드관리','Field Mgt.','STD030',2,'Y','',0,'#solution/field','Y')
,('STD056','토폴로지필드','Field ','STD036',0,'Y','',1,'#solution/field/field','Y')
,('STD057','커스텀필드','Custom Field','STD036',1,'Y','',1,'#solution/field/custom','Y')
,('STD058','인터페이스필드','Interface Field','STD036',2,'Y','',1,'#solution/field/interface','Y')
,('STD037','데이터보관관리','Storage Mgt.','STD030',3,'Y','',0,'#solution/datastore','Y')
,('STD059','DB데이터주기설정','DB Settings','STD037',0,'Y','',1,'#solution/datastore/datacycle','Y')
,('STD060','엔진로그주기설정','Engine Log Settings','STD037',1,'Y','',1,'#solution/datastore/enginecycle','Y')
,('STD061','백업설정','Backup Settings','STD037',2,'Y','',1,'#solution/datastore/backup','Y')
,('STD038','라이선스관리','License Mgt.','STD030',4,'Y','',0,'#solution/licensev2','Y')
,('STD178','라이선스관리','License Mgt.','STD038',0,'Y','',1,'#solution/licensev2/license','Y')
,('STD039','서버관리','Server Mgt.','STD030',5,'Y','',0,'#solution/serverMgt','Y')
,('STD063','서버관리','Server Mgt.','STD039',0,'Y','',1,'#solution/server','Y')
,('STD062','수집그룹','Collector Group','STD039',1,'Y','',1,'#solution/collectgroup','Y')
,('STD064','스냅샷 관리','Target snapshot Mgt.','STD039',2,'Y','',1,'#solution/snapshot','Y')
,('STD032','일반관리','General Mgt.','STD003',3,'Y','',0,'','Y')
,('STD040','화면설정','UI Settings','STD032',0,'Y','(탭) 공통 / 사용자',0,'#general/displayset','Y')
,('STD122','공통','Common UI Settings','STD040',0,'Y','',1,'#general/displayset/common','Y')
,('STD123','사용자','Users','STD040',1,'Y','',1,'#general/displayset/users','Y')
,('STD041','이벤트팝업설정','Event Popup Settings','STD032',1,'N','(탭) 이벤트 팝업 / 지속형 팝업',0,'#general/popupSetting','N')
,('STD124','이벤트 팝업','Event Popup Settings','STD041',0,'Y','',1,'#general/popupset','Y')
,('STD125','지속형 이벤트 팝업','Modeless Event Popup Settings','STD041',1,'Y','',1,'#general/modelesspopupset','Y')
,('STD042','일정관리','Calendar','STD032',2,'Y','(탭) 공휴일 / 업무시간',0,'#general/schedule','Y')
,('STD175','공휴일','Holiday','STD042',0,'Y','',1,'#general/schedule/holiday','Y')
,('STD176','업무시간','Engagement','STD042',1,'Y','',1,'#general/schedule/officehours','Y')
,('STD043','게시판관리','Board Mgt.','STD032',3,'Y','',0,'#general/board','Y')
,('STD177','게시판관리','Board Mgt.','STD043',0,'Y','',1,'#general/board/boardsetting','Y')
,('STD044','기능설정','Function Setting','STD032',5,'N','',0,'#general/funcset','N')
,('STD045','유지보수연락망','Maintenance Contact Network','STD032',6,'N','',0,'#general/contactNetwork','N')
,('STD033','보안관리','Security Mgt.','STD003',4,'Y','',0,'','Y')
,('STD046','보안정책','Security Policy','STD033',0,'Y','(탭) 로그인 / 비밀번호 / UI접속 허용설정',0,'#security/policy','Y')
,('STD117','로그인','Login Policy','STD046',0,'Y','',1,'#security/policy/login','Y')
,('STD118','비밀번호','Password Policy','STD046',1,'Y','',1,'#security/policy/login/passwd','Y')
,('STD048','UI 접속 허용설정','IP Connection Restriction','STD046',2,'Y','',1,'#security/policy/loginiprule','Y')
,('STD047','이력관리','History Mgt.','STD033',1,'Y','(탭) 작업이력 / 접속이력',0,'#security/history','Y')
,('STD120','작업이력','Task History','STD047',0,'Y','',1,'#security/history/jobhist','Y')
,('STD121','접속이력','Login History','STD047',1,'Y','',1,'#security/history/connhist','Y')
,('STD049','조직관리','','STD031',0,'Y','',0,'','Y')
,('STD200','이벤트 분석','Event Monitoring','',11,'Y','',0,'/event','Y')
,('STD201','기본뷰','Default','STD200',0,'Y','',1,'#event/default','Y')
,('STD202','서비스그룹','Group','STD200',1,'Y','',1,'#event/group','Y')
,('STD203','토폴로지','Topology','STD200',2,'Y','',1,'#event/topology','Y')
,('STD204','이벤트','Event','STD200',0,'Y','',2,'/event/monitor','N')
,('STD205','로그','System Log','STD200',1,'Y','',2,'/event/log','N')
,('STD206','실시간','Real time event','STD204',0,'Y','',3,'/event/monitor/realtime','N')
,('STD207','이벤트 이력','Event history','STD204',1,'Y','',3,'/event/monitor/history','N')
,('STD208','조치이력','Action history','STD204',2,'Y','',3,'/event/monitor/actionHistory','N')
,('STD209','예외시간','Exception time','STD204',3,'Y','',3,'/event/monitor/exceptionTime','N')
,('STD210','Syslog','Syslog','STD205',0,'Y','',3,'/event/log/syslog','N')
,('STD211','Trap','Trap','STD205',1,'Y','',3,'/event/log/trap','N')
,('STD300','SMS','SMS','',10,'Y','',0,'/sms','Y')
,('STD301','기본뷰','Default','STD300',0,'Y','',1,'#sms/default','Y')
,('STD302','서비스그룹','Group','STD300',1,'Y','',1,'#sms/group','Y')
,('STD303','토폴로지','Topology','STD300',2,'Y','',1,'#sms/topology','Y')
,('STD304','목록','List','STD300',0,'Y','',2,'/sms/list','N')
,('STD305','요약','Overview','STD300',1,'Y','',2,'/sms/overview','N')
,('STD306','운영관리','Operation Management','STD300',2,'Y','',2,'/sms/operation','N')
,('STD307','정책관리','Policy Management','STD300',3,'Y','',2,'/sms/policy','N')
,('STD308','기본','Basic','STD304',0,'Y','',3,'/sms/list/basic','N')
,('STD309','요약','Summary','STD304',1,'Y','',3,'/sms/list/summary','N')
,('STD310','성능','Performance','STD304',2,'Y','',3,'/sms/list/perf','N')
,('STD311','저장장치','Storage','STD304',3,'Y','',3,'/sms/list/storage','N')
,('STD312','프로세스','Process','STD304',4,'Y','',3,'/sms/list/process','N')
,('STD313','네트워크','Network','STD304',5,'Y','',3,'/sms/list/network','N')
,('STD314','서비스','Service','STD304',6,'Y','',3,'/sms/list/service','N')
,('STD315','에이전트','Agent','STD306',0,'Y','',3,'/sms/operation/agent','N')
,('STD316','파일','File','STD306',1,'Y','',3,'/sms/operation/file','N')
,('STD317','프로세스','Process','STD306',2,'Y','',3,'/sms/operation/process','N')
,('STD318','서비스','Service','STD306',3,'Y','',3,'/sms/operation/service','N')
,('STD319','에이전트','Agent','STD307',0,'Y','',3,'/sms/policy/agent','N')
,('STD320','이벤트','Event','STD307',1,'Y','',3,'/sms/policy/event/tmpl','N')
,('STD321','프로세스','Process','STD307',2,'Y','',3,'/sms/policy/process','N')
,('STD322','이벤트 예외시간','Event Exclusion Time','STD307',3,'Y','',3,'/sms/policy/eventExclusion','N')
,('STD323','로그수집','log','STD307',4,'Y','',3,'/sms/policy/log','N');

-- 역할별메뉴관리 tab(기본값)
INSERT INTO TMP_ROLE_MENU_BASE (MENU_ID, ROLE_ID, VIEW_PRIV, ADD_PRIV, EDIT_PRIV, DEL_PRIV) VALUES
('STD000',1,'Y','Y','Y','Y')
,('STD000',2,'Y','Y','Y','Y')
,('STD000',3,'Y','Y','N','N')
,('STD000',4,'Y','N','N','N');


-- 가능한 조합을 모두 임시테이블에 넣고 추가한다.(기준은 STD000 이다.)
INSERT INTO TMP_ROLE_MENU_PRIV (MENU_ID, ROLE_ID, VIEW_PRIV, ADD_PRIV, EDIT_PRIV, DEL_PRIV)
SELECT MENU_ID, ROLE_ID, VIEW_PRIV, ADD_PRIV, EDIT_PRIV, DEL_PRIV
FROM TMP_MENU_INFO
CROSS JOIN 
(SELECT ROLE_ID, VIEW_PRIV, ADD_PRIV, EDIT_PRIV, DEL_PRIV FROM TMP_ROLE_MENU_BASE) O;


-- 메뉴기능 기초데이터
INSERT INTO TMP_MENU_MNG (FUNC_CD, FUNC_NM, USE_YN, FUNC_GB, UI_ORDER) VALUES
('NOTI_LVL','통보설정(관리등급사용)','Y','운영현황',1)
,('PORTVIEW','포트뷰','N','운영현황',2)
,('ARP','ARP 수집','N','운영현황',3)
,('AI','AI','N','중요기능',1)
,('SVR','서버','Y','중요기능',2)
,('AP','무선','N','중요기능',3)
,('L4','L4','Y','중요기능',4)
,('UPS','UPS','N','중요기능',5)
,('TE','X.25(MegaPAC)','N','중요기능',6)
,('IPT','IPT 사용여부','N','중요기능',7)
,('STR','스토리지','N','중요기능',8)
,('TEA','전송장비','N','중요기능',9)
,('BIGEYE','BigEye 로그연동','N','타 시스템 연동',1)
,('EMS','EMS 연동','N','타 시스템 연동',2)
,('APPM','APPM 연동','N','타 시스템 연동',3)
,('MAPEVT_SEP','이벤트 카운트 분리','N','토폴로지맵',1)
,('SITEMAP','토폴로지맵 사이트맵 사용','N','토폴로지맵',2)
,('TERM','터미널','N','터미널 기능',1)
,('TERM_WEB','Client 프로그램','N','터미널 기능',2)
,('TERM_P_PUT','PUTTY','N','터미널 기능',3)
,('TERM_P_CRT','SecureCRT','Y','터미널 기능',4)
,('TMS','Flow','Y','TMS 설정',1)
,('TMS_PACKET','TMS 패킷','N','TMS 설정',2)
,('TMS_GEO','Geo 맵','N','TMS 설정',3)
,('SECLOGIN','보안 로그인','Y','UI',1)
,('HOLI_TAB','공휴일 TAB','Y','UI',2)
,('LAST_POP','최종 로그인 정보 표시','N','UI',3)
,('OFFICE_SCH','지점별 업무시간 사용여부','N','UI',4)
,('RPT_GRP','보고서 그룹 사용여부','N','UI',5)
,('RPTUSERSET','보고서 개인설정 사용','N','UI',6)
,('TOOLTIP','툴팁 표시','Y','UI',7);


  -- 없는 데이터만 추가.
  INSERT INTO MENU_INFO
  SELECT A.*
  FROM TMP_MENU_INFO A
  LEFT OUTER JOIN MENU_INFO B ON A.MENU_ID = B.MENU_ID
  WHERE B.MENU_ID IS NULL;  
  
  
  INSERT INTO ROLE_MENU_PRIV
  SELECT A.*
  FROM TMP_ROLE_MENU_PRIV A
  LEFT OUTER JOIN ROLE_MENU_PRIV B ON A.MENU_ID = B.MENU_ID AND A.ROLE_ID = B.ROLE_ID
  WHERE B.MENU_ID IS NULL;

  INSERT INTO MENU_MNG
  SELECT A.*
  FROM TMP_MENU_MNG A
  LEFT OUTER JOIN MENU_MNG B ON A.FUNC_CD = B.FUNC_CD
  WHERE B.FUNC_CD IS NULL;
 
  
  -- 저장용 임시 테이블 삭제
  DROP TEMPORARY TABLE IF EXISTS TMP_MENU_INFO;
  DROP TEMPORARY TABLE IF EXISTS TMP_ROLE_MENU_PRIV;
  DROP TEMPORARY TABLE IF EXISTS TMP_ROLE_MENU_BASE;
  DROP TEMPORARY TABLE IF EXISTS TMP_MENU_MNG;
SELECT '메뉴코드 패치 완료' AS RESULT;