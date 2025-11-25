# 누적 패치: from-1.0.0 to 1.1.1


## 개요
이 패치는 **1.0.0** 버전에서 **1.1.1** 버전으로 업그레이드하기 위한 누적 패치입니다.


## 생성 정보
- **생성일**: 2025-11-24 15:03:18
- **From Version**: 1.0.0
- **To Version**: 1.1.1
- **포함된 버전**: 1.1.0 1.1.1

## 디렉토리 구조
```
from-1.0.0/
├── mariadb/
│   ├── mariadb_patch.sh        # MariaDB 패치 실행 스크립트
│   └── source_files/           # 누적된 SQL 파일들
├── cratedb/
│   ├── cratedb_patch.sh        # CrateDB 패치 실행 스크립트
│   └── source_files/           # 누적된 SQL 파일들
└── README.md                   # 이 파일
```

## 사용 방법

### 사전 준비
1. **백업 필수**: 패치 실행 전 반드시 데이터베이스를 백업하세요.
   ```bash
   # MariaDB 백업
   cd mariadb
   ../../../common/script/mariadb/mariadb_backup.sh
   ```

2. **버전 확인**: 현재 시스템 버전이 1.0.0인지 확인하세요.    
   ```sql
    SELECT * FROM NMS_DB.VERSION_HISTORY;
    ```

### 패치 실행

#### MariaDB 패치
```bash
cd mariadb
chmod +x mariadb_patch.sh
./mariadb_patch.sh
```

#### CrateDB 패치
```bash
cd cratedb
chmod +x cratedb_patch.sh
./cratedb_patch.sh
```

## 주의사항
⚠️ **중요**: 이 패치는 여러 버전의 변경사항을 누적한 것입니다.
- 패치 실행 전 반드시 백업을 수행하세요.
- 패치 실행 중 오류 발생 시 로그를 확인하세요.

## 포함된 패치 버전 목록

### Version 1.1.0
```
VERSION: 1.1.0
CREATED_AT: 2025-10-31
CREATED_BY: jhlee@tscientific
COMMENT: 데이터코드, 이벤트코드, 메뉴코드 추가 / SMS 기능 추가 / VERSION_HISTORY 테이블 추가 / V_INFO_MCH 관련 뷰 변경
```

### Version 1.1.1
```
VERSION: 1.1.1
CREATED_AT: 2025-11-05
CREATED_BY: jhlee@tscientific
COMMENT: 운영관리 - 파일 기능 관련 테이블 추가
```


## 문제 발생 시
1. 로그 파일 확인: `mariadb/logs/`, `cratedb/logs/`
2. 백업으로 복구
3. 개발팀에 문의

---
CREATED BY. Infraeye2 누적 패치 생성기
