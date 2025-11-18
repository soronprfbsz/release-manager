# Docker 구성 파일

이 디렉토리는 프로젝트의 Docker 관련 파일들을 포함합니다.

## 📁 파일 구조

```
docker/
├── docker-compose.yml    # Docker Compose 설정
├── Dockerfile           # Spring Boot 애플리케이션 이미지 빌드
├── init-mariadb.sql     # MariaDB 초기화 SQL
└── README.md           # 이 문서
```

## 🚀 사용 방법

### 로컬 개발 환경

```bash
# 프로젝트 루트에서 실행
docker-compose -f docker/docker-compose.yml up -d

# 또는 docker 디렉토리에서 실행
cd docker
docker-compose up -d
```

### 개별 서비스 실행

```bash
# MariaDB만 실행
docker-compose -f docker/docker-compose.yml up -d mariadb

# Redis만 실행
docker-compose -f docker/docker-compose.yml up -d redis

# 애플리케이션만 실행 (의존성 무시)
docker-compose -f docker/docker-compose.yml up -d --no-deps app
```

### 컨테이너 중지 및 제거

```bash
# 모든 컨테이너 중지
docker-compose -f docker/docker-compose.yml down

# 볼륨까지 함께 제거
docker-compose -f docker/docker-compose.yml down -v
```

### 로그 확인

```bash
# 모든 컨테이너 로그
docker-compose -f docker/docker-compose.yml logs -f

# 특정 컨테이너 로그
docker-compose -f docker/docker-compose.yml logs -f app
```

## 📝 파일 설명

### docker-compose.yml
- **mariadb**: MariaDB 10.11.5 데이터베이스
- **redis**: Redis 캐시 서버
- **app**: Spring Boot 애플리케이션

### Dockerfile
Spring Boot JAR 파일을 실행하는 경량 이미지

### init-mariadb.sql
MariaDB 컨테이너 초기 실행 시 자동으로 실행되는 SQL 스크립트

## 🔧 환경변수

환경변수는 프로젝트 루트의 `.env` 파일에서 관리됩니다.
`.env.example`을 참고하여 `.env` 파일을 생성하세요.

## 🌐 네트워크

모든 컨테이너는 `network-ts` 외부 네트워크를 사용합니다.

```bash
# 네트워크 생성 (최초 1회)
docker network create network-ts
```

## ⚠️ 주의사항

- 빌드 컨텍스트가 프로젝트 루트(`..`)로 설정되어 있습니다
- `.env` 파일은 프로젝트 루트에 위치해야 합니다
- CI/CD 파이프라인은 자동으로 `docker/docker-compose.yml`을 사용합니다
