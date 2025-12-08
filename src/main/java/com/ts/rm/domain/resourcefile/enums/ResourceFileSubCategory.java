package com.ts.rm.domain.resourcefile.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리소스 파일 하위 카테고리 Enum
 *
 * <p>각 카테고리별 세부 분류를 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ResourceFileSubCategory {
    // 스크립트 하위 분류
    MARIADB("MariaDB", "MariaDB 관련 스크립트", ResourceFileCategory.SCRIPT),
    CRATEDB("CrateDB", "CrateDB 관련 스크립트", ResourceFileCategory.SCRIPT),

    // Docker 하위 분류
    SERVICE("서비스 실행", "Docker 서비스 실행 관련 파일", ResourceFileCategory.DOCKER),
    DOCKERFILE("Dockerfile", "Dockerfile 및 빌드 관련 파일", ResourceFileCategory.DOCKER),

    // 문서 하위 분류
    INFRAEYE1("Infraeye 1", "Infraeye 1 관련 문서", ResourceFileCategory.DOCUMENT),
    INFRAEYE2("Infraeye 2", "Infraeye 2 관련 문서", ResourceFileCategory.DOCUMENT),

    // 기타
    ETC("기타", "기타 분류되지 않은 파일", null);

    private final String displayName;
    private final String description;
    private final ResourceFileCategory parentCategory;

    /**
     * 특정 카테고리에 속하는지 확인
     */
    public boolean belongsTo(ResourceFileCategory category) {
        return this.parentCategory == category;
    }
}
