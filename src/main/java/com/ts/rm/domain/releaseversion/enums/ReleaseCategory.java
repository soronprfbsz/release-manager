package com.ts.rm.domain.releaseversion.enums;

import lombok.Getter;

/**
 * 릴리즈 카테고리 (릴리즈의 성격 분류)
 */
@Getter
public enum ReleaseCategory {
    /**
     * 설치본 (인스톨 버전)
     * <p>최초 설치 시 사용되는 전체 릴리즈 패키지
     * <p>패치 생성 시 이 카테고리의 버전은 제외됩니다.
     */
    INSTALL("INSTALL", "설치본"),

    /**
     * 패치본 (업데이트 버전)
     * <p>기존 버전에서 변경된 파일만 포함하는 증분 릴리즈
     * <p>패치 생성 시 이 카테고리의 버전만 포함됩니다.
     */
    PATCH("PATCH", "패치본");

    private final String code;
    private final String description;

    ReleaseCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 코드 값으로 ReleaseCategory 찾기
     */
    public static ReleaseCategory fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (ReleaseCategory category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Invalid release category code: " + code);
    }

    /**
     * 이 카테고리가 패치 생성 시 제외되어야 하는지 확인
     */
    public boolean isExcludedFromPatch() {
        return this == INSTALL;
    }
}
