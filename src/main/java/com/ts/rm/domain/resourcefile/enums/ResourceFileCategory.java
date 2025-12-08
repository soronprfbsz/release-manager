package com.ts.rm.domain.resourcefile.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리소스 파일 카테고리 Enum
 *
 * <p>리소스 파일의 대분류를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ResourceFileCategory {
    SCRIPT("스크립트", "스크립트 파일 (백업, 복원 등)"),
    DOCKER("Docker", "Docker 관련 파일 (컴포즈, Dockerfile 등)"),
    DOCUMENT("문서", "설치 가이드 및 기타 문서"),
    ETC("기타", "기타 리소스 파일");

    private final String displayName;
    private final String description;
}
