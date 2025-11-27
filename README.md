# Release Manager

소프트웨어 릴리즈 버전 및 패치 파일 관리를 위한 Spring Boot 기반 REST API 시스템

## 주요 기능

- ✅ 릴리즈 버전 관리 (표준/고객사별 커스텀 버전)
- ✅ 패치 파일 관리 (MariaDB/CrateDB SQL 스크립트)
- ✅ 누적 패치 생성 (버전 범위별)
- ✅ JWT 기반 인증 시스템
- ✅ RESTful API 설계
- ✅ Swagger API 문서화
- ✅ GitLab CI/CD + Harbor Registry

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.5.6, Spring Security |
| Language | Java 17 |
| Database | MariaDB 10.11.5, Redis |
| Query | QueryDSL, JPA, p6spy |
| Migration | Flyway |
| API Docs | SpringDoc OpenAPI 3 |
| Build | Gradle 8.5 |
| Container | Docker, Docker Compose |
| CI/CD | GitLab CI/CD, Harbor Registry |

## 빠른 시작

### 1. 환경 설정

```bash
# 저장소 클론
git clone <repository-url>
cd release-manager

# 환경 변수 설정
cp .env.example .env
# .env 파일 수정 (DB 접속 정보, JWT Secret 등)
```

### 2. Docker로 실행 (권장)

```bash
# 전체 서비스 실행
docker compose -f docker/docker-compose.yml --env-file .env up -d

# 로그 확인
docker compose -f docker/docker-compose.yml logs -f app

# 중지
docker compose -f docker/docker-compose.yml down
```

### 3. 로컬 개발 모드

```bash
# DB만 실행
docker compose -f docker/docker-compose.yml --env-file .env up -d mariadb redis

# Application 로컬 실행
./gradlew bootRun
```

### 4. API 문서

- **Swagger UI**: http://localhost:8081/swagger
- **Health Check**: http://localhost:8081/actuator/health

## 프로젝트 구조

```
src/main/java/com/ts/rm/
├── domain/                        # 도메인 계층
│   ├── auth/                      # 인증 (로그인, 회원가입, 토큰)
│   ├── releaseversion/            # 릴리즈 버전 관리
│   ├── releasefile/               # 릴리즈 파일 관리
│   ├── patch/                     # 패치 생성/조회
│   ├── customer/                  # 고객사 관리
│   └── script/                    # DB 백업/복원 스크립트
└── global/                        # 공통 기능
    ├── config/                    # 설정 (Security, Swagger, QueryDSL)
    ├── security/                  # JWT 인증 필터
    └── exception/                 # 전역 예외 처리

src/main/resources/
├── application.yml                # 애플리케이션 설정
├── db/migration/                  # Flyway 마이그레이션
└── release/                       # 릴리즈 파일 저장소
    ├── patches/                   # 생성된 패치 파일 (git 제외)
    ├── versions/                  # 버전별 SQL 스크립트
    └── templates/                 # 패치 스크립트 템플릿
```

## 주요 API

### 인증
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | 토큰 갱신 |

### 릴리즈 버전
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/releases/versions/{id}` | 버전 상세 조회 |
| GET | `/api/releases/standard/tree` | 표준 버전 트리 조회 |
| GET | `/api/releases/custom/{customer-code}/tree` | 커스텀 버전 트리 조회 |

### 릴리즈 파일
| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/releases/files/{id}/download` | 파일 다운로드 |

### 패치
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/patches` | 누적 패치 생성 |
| GET | `/api/patches` | 패치 목록 조회 |
| GET | `/api/patches/{id}` | 패치 상세 조회 |
| GET | `/api/patches/{id}/download` | 패치 다운로드 |

자세한 API 명세는 Swagger UI를 참고하세요.

## 개발 가이드

### 계층 구조

```
Controller → Service → Repository → Database
    ↓           ↓
   DTO        Entity
```

### Repository 패턴 (JPA + QueryDSL)

```java
// Spring Data JPA (기본 CRUD)
public interface ReleaseVersionRepository extends JpaRepository<ReleaseVersion, Long>,
                                                   ReleaseVersionRepositoryCustom {
    Optional<ReleaseVersion> findByVersion(String version);
}

// QueryDSL (복잡한 쿼리)
public interface ReleaseVersionRepositoryCustom {
    List<ReleaseVersion> findStandardVersions();
}

public class ReleaseVersionRepositoryImpl implements ReleaseVersionRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    // QueryDSL 구현
}
```

### 데이터베이스 마이그레이션

Flyway를 사용하여 스키마 변경을 관리합니다.

```sql
-- src/main/resources/db/migration/V8__create_new_table.sql
CREATE TABLE new_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)
);
```

애플리케이션 재시작 시 자동으로 마이그레이션이 적용됩니다.

## 빌드 및 테스트

```bash
# 빌드 (테스트 제외)
./gradlew clean build -x test

# 전체 테스트
./gradlew test

# 특정 도메인 테스트
./gradlew test --tests "com.ts.rm.domain.patch.*"

# QueryDSL Q클래스 재생성
./gradlew clean build
```

## CI/CD 파이프라인

### 파이프라인 구조

```
main 브랜치 push
  ↓
1. build-job          → Gradle 빌드
  ↓
2. test-job (수동)    → 테스트 실행
  ↓
3. docker-build-job   → Docker 이미지 빌드 (ts/release-manager-api:latest)
  ↓
4. harbor-push-job (수동) → Harbor Registry에 푸시 (latest + 커밋SHA)
  ↓
5. deploy-job         → GitLab Runner 호스트 배포
```

### Harbor 이미지 관리

- **Harbor**: `${HARBOR_REGISTRY}/ts/release-manager-api:latest`, `${HARBOR_REGISTRY}/ts/release-manager-api:{commit-sha}`
- **호스트**: `ts/release-manager-api:latest` (실행용)

### GitLab Variables 설정

Settings → CI/CD → Variables에 다음 변수 추가:

| 변수명 | 설명 |
|--------|------|
| `SERVER_NAME` | 애플리케이션 이름 |
| `SERVER_HOST` | 배포 서버 IP |
| `SERVER_EXTERNAL_PORT` | 외부 접근 포트 |
| `MARIADB_*` | MariaDB 접속 정보 |
| `REDIS_*` | Redis 접속 정보 |
| `JWT_SECRET` | JWT 시크릿 키 (최소 256비트) |
| `HARBOR_*` | Harbor Registry 접속 정보 |

**주의**: 특수문자 포함 변수는 "Expand variable reference" 비활성화

## Docker 명령어

```bash
# 시작
docker compose -f docker/docker-compose.yml --env-file .env up -d

# 로그
docker compose -f docker/docker-compose.yml logs -f app

# 재시작
docker compose -f docker/docker-compose.yml restart app

# 중지
docker compose -f docker/docker-compose.yml down
```

## 문제 해결

### QueryDSL Q클래스 생성 안됨
```bash
./gradlew clean build
```

### Flyway 마이그레이션 오류
```bash
# 마이그레이션 히스토리 확인
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

# 개발 환경에서만: 마이그레이션 초기화
./gradlew flywayClean flywayMigrate
```

### Docker 컨테이너 문제
```bash
# 컨테이너 재시작
docker compose restart app

# 로그 확인
docker compose logs --tail 100 app

# 컨테이너 상태 확인
docker compose ps
```

## 코드 스타일

**Google Java Style Guide** 사용

- IntelliJ IDEA: `Ctrl+Alt+L` (Windows) / `Cmd+Option+L` (Mac)

## 라이선스

MIT License
