package com.ts.rm.global.file;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * ZIP 파일 압축 유틸리티
 *
 * <p>디렉토리 및 파일들을 ZIP 형식으로 압축하는 공통 기능 제공
 */
@Slf4j
public class ZipUtil {

    private ZipUtil() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 디렉토리를 ZIP으로 압축
     *
     * @param sourceDir 압축할 디렉토리 경로
     * @return ZIP 파일 바이트 배열
     * @throws BusinessException 압축 실패 시
     */
    public static byte[] compressDirectory(Path sourceDir) {
        if (!Files.exists(sourceDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "압축할 디렉토리를 찾을 수 없습니다: " + sourceDir);
        }

        if (!Files.isDirectory(sourceDir)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "디렉토리가 아닙니다: " + sourceDir);
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> addFileToZip(zos, sourceDir, path));

            zos.finish();
            log.debug("디렉토리 압축 완료: {} ({} bytes)", sourceDir, baos.size());
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("디렉토리 압축 실패: {}", sourceDir, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "디렉토리 압축 실패: " + e.getMessage());
        }
    }

    /**
     * 여러 파일을 지정된 폴더 구조로 ZIP 압축
     *
     * @param files 압축할 파일 목록 (ZipFileEntry 리스트)
     * @return ZIP 파일 바이트 배열
     * @throws BusinessException 압축 실패 시
     */
    public static byte[] compressFiles(List<ZipFileEntry> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "압축할 파일이 없습니다");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            int addedFileCount = 0;
            List<String> missingFiles = new ArrayList<>();

            for (ZipFileEntry fileEntry : files) {
                if (!Files.exists(fileEntry.sourcePath())) {
                    String missingPath = fileEntry.sourcePath().toString();
                    log.warn("파일이 존재하지 않습니다: {} (ZIP 경로: {})",
                            missingPath, fileEntry.zipEntryPath());
                    missingFiles.add(missingPath);
                    continue;
                }

                String entryName = fileEntry.zipEntryPath().replace("\\", "/");
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(fileEntry.sourcePath(), zos);
                zos.closeEntry();
                addedFileCount++;
                log.debug("파일 추가: {} -> {}", fileEntry.sourcePath().getFileName(), entryName);
            }

            // 모든 파일이 존재하지 않는 경우 예외 발생
            if (addedFileCount == 0) {
                log.error("압축할 파일이 모두 존재하지 않습니다. 누락된 파일 목록: {}", missingFiles);
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "압축할 파일이 실제로 존재하지 않습니다. 누락된 파일 수: " + missingFiles.size());
            }

            // 일부 파일만 누락된 경우 경고 로그
            if (!missingFiles.isEmpty()) {
                log.warn("일부 파일이 누락되었습니다 ({}/{}개). 누락된 파일: {}",
                        missingFiles.size(), files.size(), missingFiles);
            }

            zos.finish();
            log.info("파일 압축 완료: {}개 파일 추가 (요청: {}개), {} bytes",
                    addedFileCount, files.size(), baos.size());
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("파일 압축 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일 압축 실패: " + e.getMessage());
        }
    }

    /**
     * ZIP 아카이브에 파일 추가 (내부 헬퍼 메소드)
     */
    private static void addFileToZip(ZipOutputStream zos, Path baseDir, Path filePath) {
        try {
            String entryName = baseDir.relativize(filePath).toString().replace("\\", "/");
            zos.putNextEntry(new ZipEntry(entryName));
            Files.copy(filePath, zos);
            zos.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("ZIP 압축 중 오류: " + filePath, e);
        }
    }

    /**
     * ZIP 엔트리 정보 (파일 경로와 ZIP 내 경로 매핑)
     *
     * @param sourcePath   실제 파일 경로
     * @param zipEntryPath ZIP 내부 경로 (예: mariadb/1.patch.sql)
     */
    public record ZipFileEntry(Path sourcePath, String zipEntryPath) {
    }
}
