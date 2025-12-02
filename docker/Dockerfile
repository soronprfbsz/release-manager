# ========================================
# Build Stage (로컬 개발용 - 선택적)
# ========================================
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /build

# Gradle 캐시 최적화를 위해 의존성 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 레이어)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 빌드 실행
RUN gradle clean build -x test --no-daemon

# ========================================
# Runtime Stage
# ========================================
FROM eclipse-temurin:17-jre-alpine

# 이미지 메타데이터 (dangling 이미지 식별용)
LABEL project="release-manager-api"
LABEL maintainer="ts-team"
LABEL version="1.0"

WORKDIR /app

# 타임존 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# Build 스테이지에서 빌드된 JAR 복사
COPY --from=builder /build/build/libs/*.jar app.jar

# JVM 옵션 설정 (중소규모 엔터프라이즈 표준: 1GB 고정)
# 스트리밍 방식으로 메모리 효율화되어 1GB로 충분
ENV JAVA_OPTS="-Xms1024m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
