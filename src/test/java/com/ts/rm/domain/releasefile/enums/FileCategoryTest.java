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
        assertThat(FileCategory.fromCode("DATABASE")).isEqualTo(FileCategory.DATABASE);
        assertThat(FileCategory.fromCode("WEB")).isEqualTo(FileCategory.WEB);
        assertThat(FileCategory.fromCode("ENGINE")).isEqualTo(FileCategory.ENGINE);
        assertThat(FileCategory.fromCode("ETC")).isEqualTo(FileCategory.ETC);
    }

    @Test
    @DisplayName("fromCode - 대소문자 무관")
    void fromCode_CaseInsensitive() {
        assertThat(FileCategory.fromCode("DATABASE")).isEqualTo(FileCategory.DATABASE);
        assertThat(FileCategory.fromCode("WEB")).isEqualTo(FileCategory.WEB);
        assertThat(FileCategory.fromCode("Engine")).isEqualTo(FileCategory.ENGINE);
        assertThat(FileCategory.fromCode("etc")).isEqualTo(FileCategory.ETC);
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
        assertThat(FileCategory.ETC.isBuildArtifact()).isFalse();
    }


    @Test
    @DisplayName("getCode - 코드 값 확인")
    void getCode() {
        assertThat(FileCategory.DATABASE.getCode()).isEqualTo("DATABASE");
        assertThat(FileCategory.WEB.getCode()).isEqualTo("WEB");
        assertThat(FileCategory.ENGINE.getCode()).isEqualTo("ENGINE");
        assertThat(FileCategory.ETC.getCode()).isEqualTo("ETC");
    }

    @Test
    @DisplayName("getDescription - 설명 값 확인")
    void getDescription() {
        assertThat(FileCategory.DATABASE.getDescription()).isEqualTo("데이터베이스");
        assertThat(FileCategory.WEB.getDescription()).isEqualTo("웹 애플리케이션");
        assertThat(FileCategory.ENGINE.getDescription()).isEqualTo("엔진");
        assertThat(FileCategory.ETC.getDescription()).isEqualTo("기타");
    }
}
