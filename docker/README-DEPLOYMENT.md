# Release Manager - Docker 배포 가이드

## 🚀 도커 환경 배포

### 전제 조건
- Docker 및 Docker Compose 설치
- `network-ts` 외부 네트워크 생성 필요

### 네트워크 생성
```bash
docker network create network-ts
```

### 환경 변수 설정

`.env` 파일에서 다음 설정 확인:

```bash
# 릴리즈 파일 저장 경로 (도커 환경)
RELEASE_BASE_PATH=/app/release-files

# 데이터베이스 연결 (도커 환경에서 자동 오버라이드)
MARIADB_HOST=mariadb    # docker-compose.yml에서 자동 설정
MARIADB_PORT=3306       # docker-compose.yml에서 자동 설정
```

### 볼륨 구성

Docker Compose는 다음 영구 볼륨을 생성합니다:

- **`release_files`**: 릴리즈 파일 및 SQL 스크립트 저장소
  - 컨테이너 경로: `/app/release-files`
  - 구조: `/app/release-files/releases/standard/`

- **`mariadb_data`**: MariaDB 데이터
- **`mariadb_log`**: MariaDB 로그
- **`redis_data`**: Redis 데이터

### 배포 방법

#### 1. 빌드 및 시작
```bash
cd docker
docker compose up -d --build
```

#### 2. 로그 확인
```bash
docker compose logs -f app
```

#### 3. 상태 확인
```bash
# 헬스체크
curl http://localhost:18080/actuator/health

# Swagger UI
http://localhost:18080/swagger
```

### 트러블슈팅

#### 파일 저장소 초기화 실패
**에러**: `파일 저장소를 초기화할 수 없습니다: /app/release-files`

**원인**: 볼륨 권한 문제 또는 디스크 공간 부족

**해결**:
```bash
# 볼륨 삭제 후 재생성
docker compose down -v
docker compose up -d
```

#### 볼륨 데이터 확인
```bash
# 볼륨 목록
docker volume ls | grep release

# 볼륨 상세 정보
docker volume inspect docker_release_files

# 컨테이너 내부에서 확인
docker exec -it release-manager ls -la /app/release-files
```

### 데이터 백업

```bash
# 릴리즈 파일 백업
docker run --rm -v docker_release_files:/data -v $(pwd):/backup \
  alpine tar czf /backup/release_files_backup.tar.gz -C /data .

# 복원
docker run --rm -v docker_release_files:/data -v $(pwd):/backup \
  alpine tar xzf /backup/release_files_backup.tar.gz -C /data
```

## 📝 환경별 설정

### 로컬 개발 (IDE)
- **경로**: `src/main/resources/release`
- **자동 생성**: IDE 실행 시 자동 생성
- **환경 변수**: `RELEASE_BASE_PATH` 미설정 (기본값 사용)

### 도커 개발/운영
- **경로**: `/app/release-files` (컨테이너 내부)
- **영구 저장**: Docker 볼륨으로 관리
- **환경 변수**: `RELEASE_BASE_PATH=/app/release-files`

### CI/CD 파이프라인
- `.env` 파일에서 `RELEASE_BASE_PATH` 확인
- 도커 이미지 빌드 시 환경 변수 자동 주입
- 배포 시 볼륨 자동 생성 및 마운트

## 🔧 설정 파일

### application.yml
```yaml
app:
  release:
    base-path: ${RELEASE_BASE_PATH:src/main/resources/release}
```
- 환경 변수 우선, 없으면 로컬 경로 사용

### docker-compose.yml
```yaml
services:
  app:
    volumes:
      - release_files:/app/release-files
    environment:
      RELEASE_BASE_PATH: /app/release-files
```

### .env
```bash
RELEASE_BASE_PATH=/app/release-files
```
