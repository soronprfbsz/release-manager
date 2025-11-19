# Spring Boot Boilerplate

RESTful API 개발을 위한 Spring Boot 3.5.6 기반 보일러플레이트 프로젝트
Account CRUD 예제 포함

## 주요 기능

- ✅ RESTful API (Account CRUD 예제)
- ✅ Spring Data JPA + QueryDSL
- ✅ Flyway 데이터베이스 마이그레이션
- ✅ Swagger API 문서화
- ✅ Docker 컨테이너 지원
- ✅ GitLab CI/CD 파이프라인

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.5.6, Spring Web, Spring Data JPA |
| Language | Java 17 |
| Database | MariaDB, Redis |
| Query | QueryDSL, p6spy (SQL 로깅) |
| Migration | Flyway |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Build | Gradle 8.5 |
| Container | Docker, Docker Compose |
| CI/CD | GitLab CI/CD |

## 빠른 시작

### 1. 환경 설정

```bash
# 저장소 클론
git clone <repository-url>
cd release-manager

# 환경 변수 설정
cp .env.example .env
# .env 파일 수정 (DB 비밀번호 등)
```

### 2. Docker로 실행 (권장)

```bash
# .env 파일을 docker/ 디렉토리로 복사
cp .env docker/.env

# MariaDB, Redis, Application 모두 실행
cd docker
docker compose up -d

# 로그 확인
docker compose logs -f app

# 중지
docker compose down
```

### 3. 로컬에서 실행

```bash
# .env 파일을 docker/ 디렉토리로 복사
cp .env docker/.env

# MariaDB, Redis만 Docker로 실행
cd docker
docker compose up -d mariadb redis

# Application은 로컬에서 실행
cd ..
./gradlew bootRun
```

### 4. API 확인

- **Swagger UI**: http://localhost:8081/swagger
- **Health Check**: http://localhost:8081/actuator/health

## 프로젝트 구조

```
src/main/java/com/rm/
├── domain/                 # 도메인별 비즈니스 로직
│   └── account/
│       ├── controller/     # REST API 엔드포인트
│       ├── service/        # 비즈니스 로직
│       ├── repository/     # 데이터 액세스 (JPA + QueryDSL)
│       ├── entity/         # JPA 엔티티
│       ├── dto/            # Request/Response DTO
│       └── enums/          # Enum 정의
└── global/                 # 공통 기능
    ├── config/             # 설정 (Swagger, QueryDSL 등)
    ├── exception/          # 예외 처리
    └── entity/             # 공통 엔티티 (BaseEntity)

src/main/resources/
├── application.yml         # 애플리케이션 설정
└── db/migration/           # Flyway 마이그레이션 SQL
```

## 개발 가이드

### 계층 구조

```
Controller → Service → Repository → Database
    ↓           ↓
   DTO        Entity
```

### Repository 패턴

```java
// Spring Data JPA (간단한 CRUD)
public interface AccountRepository extends JpaRepository<Account, Long>,
                                           AccountRepositoryCustom {
    Optional<Account> findByEmail(String email);
}

// QueryDSL (복잡한 쿼리)
public interface AccountRepositoryCustom {
    long updateAccountNameByAccountId(Long accountId, String name);
}

public class AccountRepositoryImpl implements AccountRepositoryCustom {
    // QueryDSL 구현
}
```

### 데이터베이스 변경

**Flyway 마이그레이션 파일 작성**:
```sql
-- src/main/resources/db/migration/V1__create_account_table.sql
CREATE TABLE account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    ...
);
```

**Entity 작성**:
```java
@Entity
public class Account extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    private String email;
}
```

애플리케이션 재시작 시 자동 적용됩니다.

## API 예제

### Account CRUD

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/accounts` | 계정 생성 |
| GET | `/api/accounts/{id}` | 계정 조회 |
| GET | `/api/accounts` | 계정 목록 조회 |
| PUT | `/api/accounts/{id}` | 계정 수정 |
| DELETE | `/api/accounts/{id}` | 계정 삭제 |

**요청 예시**:
```bash
# 계정 생성
curl -X POST http://localhost:8081/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "accountName": "홍길동",
    "password": "password123"
  }'

# 계정 조회
curl http://localhost:8081/api/accounts/1
```

자세한 API 명세는 Swagger UI에서 확인하세요.


## 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 도메인 테스트
./gradlew test --tests "com.ts.rm.domain.account.*"

# 테스트 커버리지
./gradlew test jacocoTestReport
```

## Docker 가이드

### Docker Compose 구조

```yaml
services:
  mariadb:       # MariaDB 10.11.5
  redis:         # Redis latest
  app:           # Spring Boot Application
```

### 주요 명령어

```bash
# 전체 시작
docker compose up -d

# 특정 서비스만 시작
docker compose up -d mariadb redis

# 로그 확인
docker compose logs -f app

# 컨테이너 상태 확인
docker compose ps

# 전체 중지 및 제거
docker compose down
```

## CI/CD 배포

### GitLab CI/CD 파이프라인

```
main 브랜치 push
  ↓
1. build       - Gradle 빌드
  ↓
2. test        - 테스트 실행 (수동)
  ↓
3. docker-build - Docker 이미지 빌드
  ↓
4. deploy      - GitLab Runner 호스트 배포
```

### GitLab Variables 설정

Settings → CI/CD → Variables에 다음 변수 추가:

| 변수명 | 값 예시 | 설명 |
|--------|---------|------|
| `SERVER_NAME` | `rm` | 애플리케이션 이름 |
| `SERVER_HOST` | `10.230.1.17` | 서버 IP |
| `SERVER_PORT` | `8081` | 애플리케이션 포트 |
| `MARIADB_*` | - | MariaDB 접속 정보 |
| `REDIS_*` | - | Redis 접속 정보 |

**주의**: 특수문자 포함 비밀번호는 "Expand variable reference" 비활성화

## 문제 해결

### QueryDSL Q클래스 생성 안됨
```bash
./gradlew clean build
```

### Flyway 마이그레이션 오류
```bash
# 마이그레이션 초기화
./gradlew flywayClean flywayMigrate
```

### Docker 컨테이너 재시작
```bash
docker compose restart app
docker compose logs -f app
```

## 코드 스타일

**Google Java Style Guide** 사용

IntelliJ IDEA:
1. `File` → `Settings` → `Editor` → `Code Style` → `Java`
2. Scheme: **GoogleStyle** 확인
3. 포맷팅: `Ctrl+Alt+L` (Windows/Linux) 또는 `Cmd+Option+L` (macOS)

## 참고 자료

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [QueryDSL Reference](http://querydsl.com/static/querydsl/latest/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)

## 라이선스

MIT License - 자유롭게 사용 가능
