package com.ts.rm.domain.releasefile.service;

import com.ts.rm.global.file.StreamingZipUtil;
import com.ts.rm.global.file.StreamingZipUtil.ZipFileEntry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스트리밍 ZIP 압축 테스트
 *
 * <p>스트리밍 방식으로 ZIP 파일을 생성하고 유효성을 검증합니다.
 */
@DisplayName("스트리밍 ZIP 압축 테스트")
class StreamingZipTest {

    @TempDir
    Path tempDir;

    private List<ZipFileEntry> testFiles;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 파일 생성
        testFiles = new ArrayList<>();

        // 1. SQL 파일
        Path sqlFile = tempDir.resolve("test.sql");
        Files.writeString(sqlFile, "SELECT * FROM test_table;");
        testFiles.add(new ZipFileEntry(sqlFile, "database/mariadb/test.sql"));

        // 2. 텍스트 파일
        Path txtFile = tempDir.resolve("readme.txt");
        Files.writeString(txtFile, "This is a test file for streaming ZIP compression.");
        testFiles.add(new ZipFileEntry(txtFile, "install/readme.txt"));

        // 3. 큰 파일 (10MB) - 메모리 효율성 테스트
        Path largeFile = tempDir.resolve("large.dat");
        byte[] largeData = new byte[10 * 1024 * 1024]; // 10MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        Files.write(largeFile, largeData);
        testFiles.add(new ZipFileEntry(largeFile, "web/large.dat"));
    }

    @Test
    @DisplayName("스트리밍 방식으로 ZIP 생성 성공")
    void streamingZipCreation() throws IOException {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        StreamingZipUtil.compressFilesToStream(outputStream, testFiles);

        // Then
        byte[] zipBytes = outputStream.toByteArray();
        assertThat(zipBytes).isNotEmpty();

        // ZIP 파일 유효성 검증
        List<String> entries = extractZipEntries(zipBytes);
        assertThat(entries).hasSize(3);
        assertThat(entries).contains(
                "database/mariadb/test.sql",
                "install/readme.txt",
                "web/large.dat"
        );
    }

    @Test
    @DisplayName("ZIP 파일 내용 검증")
    void verifyZipFileContent() throws IOException {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        StreamingZipUtil.compressFilesToStream(outputStream, testFiles);
        byte[] zipBytes = outputStream.toByteArray();

        // Then - ZIP 엔트리 내용 검증
        assertThat(extractZipContent(zipBytes, "database/mariadb/test.sql"))
                .isEqualTo("SELECT * FROM test_table;");
        assertThat(extractZipContent(zipBytes, "install/readme.txt"))
                .isEqualTo("This is a test file for streaming ZIP compression.");
    }

    @Test
    @DisplayName("대용량 파일 스트리밍 압축 성공")
    void largeFileStreamingCompression() throws IOException {
        // Given
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When
        StreamingZipUtil.compressFilesToStream(outputStream, testFiles);

        // Then - 압축 성공하고 OOM 발생하지 않음
        byte[] zipBytes = outputStream.toByteArray();
        assertThat(zipBytes.length).isGreaterThan(1024); // 최소 1KB 이상

        // 큰 파일도 정상적으로 ZIP에 포함되었는지 확인
        List<String> entries = extractZipEntries(zipBytes);
        assertThat(entries).contains("web/large.dat");
    }

    @Test
    @DisplayName("빈 파일 목록 - 예외 발생")
    void emptyFileList() {
        // Given
        List<ZipFileEntry> emptyList = new ArrayList<>();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.ts.rm.global.exception.BusinessException.class,
                () -> StreamingZipUtil.compressFilesToStream(outputStream, emptyList)
        );
    }

    /**
     * ZIP 파일에서 엔트리 목록 추출
     */
    private List<String> extractZipEntries(byte[] zipBytes) throws IOException {
        List<String> entries = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(
                new java.io.ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
                zis.closeEntry();
            }
        }
        return entries;
    }

    /**
     * ZIP 파일에서 특정 엔트리의 내용 추출
     */
    private String extractZipContent(byte[] zipBytes, String entryName) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(
                new java.io.ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(entryName)) {
                    byte[] content = zis.readAllBytes();
                    return new String(content);
                }
                zis.closeEntry();
            }
        }
        return null;
    }
}
