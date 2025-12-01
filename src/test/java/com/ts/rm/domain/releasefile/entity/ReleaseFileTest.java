package com.ts.rm.domain.releasefile.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.releasefile.enums.FileCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReleaseFile Entity 테스트
 */
class ReleaseFileTest {

    @Test
    @DisplayName("isExcludedFromPatch - INSTALL 카테고리는 패치에서 제외")
    void isExcludedFromPatch_InstallCategory() {
        // Given
        ReleaseFile installFile = ReleaseFile.builder()
                .fileCategory(FileCategory.INSTALL)
                .fileName("설치가이드.pdf")
                .build();

        // When
        boolean excluded = installFile.isExcludedFromPatch();

        // Then
        assertThat(excluded).isTrue();
    }

    @Test
    @DisplayName("isExcludedFromPatch - DATABASE 카테고리는 패치에 포함")
    void isExcludedFromPatch_DatabaseCategory() {
        // Given
        ReleaseFile databaseFile = ReleaseFile.builder()
                .fileCategory(FileCategory.DATABASE)
                .fileName("migration.sql")
                .build();

        // When
        boolean excluded = databaseFile.isExcludedFromPatch();

        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("isExcludedFromPatch - WEB 카테고리는 패치에 포함")
    void isExcludedFromPatch_WebCategory() {
        // Given
        ReleaseFile webFile = ReleaseFile.builder()
                .fileCategory(FileCategory.WEB)
                .fileName("app.war")
                .build();

        // When
        boolean excluded = webFile.isExcludedFromPatch();

        // Then
        assertThat(excluded).isFalse();
    }

    @Test
    @DisplayName("isExcludedFromPatch - 카테고리가 null이면 패치에 포함")
    void isExcludedFromPatch_NullCategory() {
        // Given
        ReleaseFile fileWithoutCategory = ReleaseFile.builder()
                .fileCategory(null)
                .fileName("legacy.sql")
                .build();

        // When
        boolean excluded = fileWithoutCategory.isExcludedFromPatch();

        // Then
        assertThat(excluded).isFalse();
    }
}
