# 누적 패치: from-1.0.0 to 1.1.0

## 개요
이 패치는 **1.0.0** 버전에서 **1.1.0** 버전으로 업그레이드하기 위한 누적 패치입니다.

## 생성 정보
- **생성일**: 2025-11-27 11:40:55
- **From Version**: 1.0.0
- **To Version**: 1.1.0
- **포함된 버전**: 1.1.0

## 디렉토리 구조
```
.
├── mariadb/
│   ├── mariadb_patch.sh        # MariaDB 패치 실행 스크립트
│   └── source_files/           # 누적된 SQL 파일들
├── cratedb/
│   ├── cratedb_patch.sh        # CrateDB 패치 실행 스크립트
│   └── source_files/           # 누적된 SQL 파일들
└── README.md                   # 이 파일
```

## 주의사항
⚠️ **중요**: 이 패치는 여러 버전의 변경사항을 누적한 것입니다.
- 패치 실행 전 반드시 백업을 수행하세요.
- 패치 실행 중 오류 발생 시 로그를 확인하세요.

---
CREATED BY. Infraeye2 누적 패치 생성기 (Java)
