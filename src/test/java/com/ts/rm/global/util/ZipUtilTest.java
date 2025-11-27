package com.ts.rm.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.file.ZipUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * ZipUtil 단위 테스트
 */
@DisplayName("ZipUtil 테스트")
class ZipUtilTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("디렉토리 압축 성공")
    void compressDirectory_Success() throws IOException {
        // Given: 임시 디렉토리에 테스트 파일 생성
        Path testFile1 = tempDir.resolve("test1.txt");
        Path testFile2 = tempDir.resolve("test2.txt");
        Files.writeString(testFile1, "Test Content 1");
        Files.writeString(testFile2, "Test Content 2");

        // When: 디렉토리 압축
        byte[] zipBytes = ZipUtil.compressDirectory(tempDir);

        // Then: ZIP 파일 검증
        assertThat(zipBytes).isNotEmpty();
        assertThat(zipBytes.length).isGreaterThan(0);

        // ZIP 파일 내용 검증
        List<String> entryNames = extractZipEntryNames(zipBytes);
        assertThat(entryNames).contains("test1.txt", "test2.txt");
    }

    @Test
    @DisplayName("존재하지 않는 디렉토리 압축 시 예외 발생")
    void compressDirectory_NotFound() {
        // Given: 존재하지 않는 경로
        Path nonExistentPath = tempDir.resolve("nonexistent");

        // When & Then: BusinessException 발생
        assertThatThrownBy(() -> ZipUtil.compressDirectory(nonExistentPath))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("압축할 디렉토리를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("파일 목록 압축 성공 (폴더 구조 유지)")
    void compressFiles_WithFolderStructure() throws IOException {
        // Given: 여러 파일과 ZIP 엔트리 목록 생성
        Path file1 = tempDir.resolve("file1.sql");
        Path file2 = tempDir.resolve("file2.sql");
        Files.writeString(file1, "SELECT * FROM table1;");
        Files.writeString(file2, "SELECT * FROM table2;");

        List<ZipUtil.ZipFileEntry> entries = new ArrayList<>();
        entries.add(new ZipUtil.ZipFileEntry(file1, "mariadb/1.patch.sql"));
        entries.add(new ZipUtil.ZipFileEntry(file2, "cratedb/1.patch.sql"));

        // When: 파일 압축
        byte[] zipBytes = ZipUtil.compressFiles(entries);

        // Then: ZIP 파일 검증
        assertThat(zipBytes).isNotEmpty();

        List<String> entryNames = extractZipEntryNames(zipBytes);
        assertThat(entryNames).containsExactlyInAnyOrder(
                "mariadb/1.patch.sql",
                "cratedb/1.patch.sql"
        );
    }

    @Test
    @DisplayName("빈 파일 목록 압축 시 예외 발생")
    void compressFiles_EmptyList() {
        // Given: 빈 리스트
        List<ZipUtil.ZipFileEntry> emptyList = new ArrayList<>();

        // When & Then: BusinessException 발생
        assertThatThrownBy(() -> ZipUtil.compressFiles(emptyList))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("압축할 파일이 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 파일 압축 시 건너뜀")
    void compressFiles_SkipNonExistentFile() throws IOException {
        // Given: 존재하는 파일과 존재하지 않는 파일
        Path existingFile = tempDir.resolve("existing.sql");
        Path nonExistentFile = tempDir.resolve("nonexistent.sql");
        Files.writeString(existingFile, "SELECT 1;");

        List<ZipUtil.ZipFileEntry> entries = new ArrayList<>();
        entries.add(new ZipUtil.ZipFileEntry(existingFile, "file1.sql"));
        entries.add(new ZipUtil.ZipFileEntry(nonExistentFile, "file2.sql"));

        // When: 파일 압축 (존재하지 않는 파일은 경고 로그 후 건너뜀)
        byte[] zipBytes = ZipUtil.compressFiles(entries);

        // Then: 존재하는 파일만 압축됨
        List<String> entryNames = extractZipEntryNames(zipBytes);
        assertThat(entryNames).containsExactly("file1.sql");
    }

    /**
     * ZIP 바이트 배열에서 엔트리 이름 목록 추출 (테스트 헬퍼)
     */
    private List<String> extractZipEntryNames(byte[] zipBytes) throws IOException {
        List<String> entryNames = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new java.io.ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
            }
        }
        return entryNames;
    }
}
