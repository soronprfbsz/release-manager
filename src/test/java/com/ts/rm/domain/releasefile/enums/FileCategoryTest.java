package com.ts.rm.domain.releasefile.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FileCategory Enum 테스트
 */
class FileCategoryTest {

    @Test
    @DisplayName("fromCode - 정상 케이스")
    void fromCode_Success() {
        assertThat(FileCategory.fromCode("database")).isEqualTo(FileCategory.DATABASE);
        assertThat(FileCategory.fromCode("web")).isEqualTo(FileCategory.WEB);
        assertThat(FileCategory.fromCode("install")).isEqualTo(FileCategory.INSTALL);
        assertThat(FileCategory.fromCode("engine")).isEqualTo(FileCategory.ENGINE);
    }

    @Test
    @DisplayName("fromCode - 대소문자 무관")
    void fromCode_CaseInsensitive() {
        assertThat(FileCategory.fromCode("DATABASE")).isEqualTo(FileCategory.DATABASE);
        assertThat(FileCategory.fromCode("WEB")).isEqualTo(FileCategory.WEB);
        assertThat(FileCategory.fromCode("Engine")).isEqualTo(FileCategory.ENGINE);
    }

    @Test
    @DisplayName("fromCode - null 입력 시 null 반환")
    void fromCode_NullInput() {
        assertThat(FileCategory.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode - 유효하지 않은 코드 입력 시 예외 발생")
    void fromCode_InvalidCode() {
        String invalidCode = "invalid";

        assertThatThrownBy(() -> FileCategory.fromCode(invalidCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file category code");
    }

    @Test
    @DisplayName("isBuildArtifact - 빌드 산출물 카테고리 확인")
    void isBuildArtifact() {
        assertThat(FileCategory.WEB.isBuildArtifact()).isTrue();
        assertThat(FileCategory.ENGINE.isBuildArtifact()).isTrue();

        assertThat(FileCategory.DATABASE.isBuildArtifact()).isFalse();
        assertThat(FileCategory.INSTALL.isBuildArtifact()).isFalse();
    }

    @Test
    @DisplayName("isExcludedFromPatch - 패치 제외 카테고리 확인")
    void isExcludedFromPatch() {
        assertThat(FileCategory.INSTALL.isExcludedFromPatch()).isTrue();

        assertThat(FileCategory.DATABASE.isExcludedFromPatch()).isFalse();
        assertThat(FileCategory.WEB.isExcludedFromPatch()).isFalse();
        assertThat(FileCategory.ENGINE.isExcludedFromPatch()).isFalse();
    }

    @Test
    @DisplayName("getCode - 코드 값 확인")
    void getCode() {
        assertThat(FileCategory.DATABASE.getCode()).isEqualTo("DATABASE");
        assertThat(FileCategory.WEB.getCode()).isEqualTo("WEB");
        assertThat(FileCategory.INSTALL.getCode()).isEqualTo("INSTALL");
        assertThat(FileCategory.ENGINE.getCode()).isEqualTo("ENGINE");
    }

    @Test
    @DisplayName("getDescription - 설명 값 확인")
    void getDescription() {
        assertThat(FileCategory.DATABASE.getDescription()).isEqualTo("데이터베이스");
        assertThat(FileCategory.WEB.getDescription()).isEqualTo("웹 애플리케이션");
        assertThat(FileCategory.INSTALL.getDescription()).isEqualTo("설치본");
        assertThat(FileCategory.ENGINE.getDescription()).isEqualTo("엔진");
    }
}
