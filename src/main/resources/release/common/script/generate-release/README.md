# 누적 패치 생성기

고객사 버전에서 최신 버전까지의 누적 패치를 자동 생성합니다.

## 사용법

```bash
cd common/script/release-generate
./generate_release.sh --from 1.0.0 --to 1.1.1
```

또는 대화형 실행:
```bash
./generate_release.sh
```

## 패치 실행 방식

생성된 `mariadb_patch.sh`는 두 가지 방식으로 실행 가능합니다:

### 1. 로컬 Docker 컨테이너
- Docker 환경에서 실행
- 컨테이너 이름으로 자동 연결

### 2. 원격 MariaDB 서버
- 호스트 주소, 포트 입력
- `mariadb` 클라이언트 필요
- 방화벽 설정 확인 필요

## 입력 (개발자가 작성)

```
releases/standard/1.1.x/1.1.0/patch/
├── mariadb/
│   ├── 1.patch_ddl.sql
│   ├── 2.patch_view.sql
│   └── ...
└── cratedb/
    └── 1.patch_ddl.sql
```

## 출력 (스크립트 자동 생성)

```
releases/standard/1.1.x/1.1.1/from-1.0.0/
├── README.md
├── mariadb/
│   ├── mariadb_patch.sh       # 자동 생성
│   └── source_files/
│       ├── 1.1.0/              # 버전별로 정렬됨
│       └── 1.1.1/
└── cratedb/
    ├── cratedb_patch.sh        # 자동 생성
    └── source_files/
```

## 실행 순서

**버전 순서 → 파일명 순서로 자동 정렬됩니다.**

예시:
```
1. [1.1.0] 1.patch_ddl.sql
2. [1.1.0] 2.patch_view.sql
3. [1.1.1] 1.patch_dml.sql
4. [1.1.1] 2.hotfix.sql
```

## 주요 기능

- ✅ 모든 `.sql` 파일 자동 포함
- ✅ 버전 순서 자동 보장
- ✅ `patch_note.md`에서 메타데이터 자동 추출
- ✅ 패치 실행 시 VERSION_HISTORY 자동 등록
  - `STANDARD_VERSION`: 자동 설정 (standard) 또는 `patch_note.md`에서 추출 (custom)
  - `CUSTOM_VERSION`: `patch_note.md`에서 추출 (custom인 경우)
  - `SYSTEM_APPLIED_BY`: 패치 실행 시 입력
  - `SYSTEM_APPLIED_AT`: CURRENT_TIMESTAMP() 자동 설정

## 주의사항

- ⚠️ `patch_note.md`에 버전 정보 필수 작성
- ⚠️ SQL 파일에 VERSION_HISTORY INSERT 작성 금지 (자동 처리됨)
- ⚠️ 생성된 패치파일은 로컬 환경에서 반드시 검증 
