# Docker 구성

Release Manager Docker Compose 설정

## 📁 파일

```
docker/
├── docker-compose.yml     # 서비스 구성
├── Dockerfile.ci          # CI/CD용 이미지
└── init-mariadb.sql       # MariaDB 초기화
```

## 🚀 실행

**프로젝트 루트에서 실행하세요.**

```bash
# 시작
docker compose -f docker/docker-compose.yml --env-file .env up -d

# 로그
docker compose -f docker/docker-compose.yml logs -f app

# 중지
docker compose -f docker/docker-compose.yml down
```

## 🔧 서비스

| 서비스 | 포트 | 설명 |
|--------|------|------|
| mariadb | 13306:3306 | MariaDB 10.11.5 |
| redis | 16379:6379 | Redis 8.2.3 |
| app | 8081:8080 | Spring Boot |

## 📝 환경 변수

프로젝트 루트의 `.env` 파일에서 관리합니다.

```bash
# 서버
SERVER_NAME=release-manager-api
SERVER_PORT=8080
SERVER_EXTERNAL_PORT=8081

# MariaDB
MARIADB_HOST=mariadb
MARIADB_PORT=3306
MARIADB_DATABASE=release_manager
MARIADB_ROOT_PASSWORD=password
MARIADB_USERNAME=root
MARIADB_PASSWORD=password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=password

# JWT
JWT_SECRET=your-secret-key-256-bits
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000

# Flyway
FLYWAY_ENABLED=true
```

## 🌐 네트워크

```bash
# 최초 1회 생성
docker network create network-ts
```

## 🐛 문제 해결

### 환경 변수 경고

**증상**: `The "MARIADB_PORT" variable is not set`

**해결**: `--env-file .env` 옵션 추가
```bash
# ✅ 올바름
docker compose -f docker/docker-compose.yml --env-file .env up -d

# ❌ 오류 (env-file 없음)
docker compose -f docker/docker-compose.yml up -d
```

### 컨테이너 시작 실패

```bash
# 로그 확인
docker compose -f docker/docker-compose.yml logs --tail 100 app

# 강제 재생성
docker compose -f docker/docker-compose.yml --env-file .env up -d --force-recreate
```

### MariaDB/Redis 연결 실패

```bash
# MariaDB 헬스 체크
docker exec release-manager-mariadb healthcheck.sh --connect

# Redis 연결 테스트
docker exec release-manager-redis redis-cli -a "password" ping
```
