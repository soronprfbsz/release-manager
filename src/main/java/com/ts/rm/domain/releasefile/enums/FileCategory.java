package com.ts.rm.domain.releasefile.enums;

import lombok.Getter;

/**
 * 릴리즈 파일 카테고리 (기능적 대분류)
 */
@Getter
public enum FileCategory {
    /**
     * 데이터베이스 관련 파일
     * <p>Sub-category: MARIADB, CRATEDB, METADATA
     */
    DATABASE("DATABASE", "데이터베이스"),

    /**
     * 웹 애플리케이션 파일
     * <p>Sub-category: BUILD, WEBOBJECTS, METADATA, ETC
     */
    WEB("WEB", "웹 애플리케이션"),

    /**
     * 설치 관련 파일
     * <p>Sub-category: SH, IMAGE, METADATA, ETC
     * <p>패치 생성 시 이 카테고리의 파일은 제외됩니다.
     */
    INSTALL("INSTALL", "설치본"),

    /**
     * 엔진 관련 파일
     * <p>Sub-category: BUILD, SH, IMAGE, METADATA, ETC
     */
    ENGINE("ENGINE", "엔진");

    private final String code;
    private final String description;

    FileCategory(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 코드 값으로 FileCategory 찾기
     */
    public static FileCategory fromCode(String code) {
        if (code == null) {
            return null;
        }

        for (FileCategory category : values()) {
            if (category.code.equalsIgnoreCase(code)) {
                return category;
            }
        }

        throw new IllegalArgumentException("Invalid file category code: " + code);
    }

    /**
     * 이 카테고리가 빌드 산출물 카테고리인지 확인
     */
    public boolean isBuildArtifact() {
        return this == WEB || this == ENGINE;
    }

    /**
     * 이 카테고리가 패치 생성 시 제외되어야 하는지 확인
     */
    public boolean isExcludedFromPatch() {
        return this == INSTALL;
    }
}
