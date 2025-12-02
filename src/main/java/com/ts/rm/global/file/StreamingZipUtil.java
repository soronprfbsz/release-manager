package com.ts.rm.global.file;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;

/**
 * 스트리밍 방식 ZIP 압축 유틸리티
 *
 * <p>메모리 사용량을 최소화하면서 HTTP 응답 스트림에 직접 ZIP 파일을 생성합니다.
 * ByteArrayOutputStream 대신 응답 OutputStream을 직접 사용하여 메모리 효율성을 극대화합니다.
 *
 * <p><b>장점:</b>
 * <ul>
 *   <li>메모리 사용량: O(1) - 버퍼 크기만 사용 (기본 8KB)</li>
 *   <li>실시간 스트리밍: 압축과 동시에 전송 시작</li>
 *   <li>대용량 파일 안전: 파일 크기에 관계없이 안정적 동작</li>
 *   <li>동시 요청 안전: 여러 다운로드 요청에도 메모리 안정성 유지</li>
 * </ul>
 *
 * @see ZipUtil 기존 메모리 기반 압축 (레거시)
 */
@Slf4j
public class StreamingZipUtil {

    private static final int BUFFER_SIZE = 8192; // 8KB 버퍼

    private StreamingZipUtil() {
        // Utility class - 인스턴스 생성 방지
    }

    /**
     * 여러 파일을 스트리밍 방식으로 ZIP 압축하여 OutputStream에 직접 작성
     *
     * <p>이 메서드는 메모리에 전체 ZIP을 생성하지 않고, 각 파일을 읽으면서
     * 즉시 압축하여 출력 스트림으로 전송합니다.
     *
     * @param outputStream 압축된 데이터를 쓸 출력 스트림 (예: HttpServletResponse.getOutputStream())
     * @param files        압축할 파일 목록 (ZipFileEntry 리스트)
     * @throws BusinessException 압축 실패 시
     */
    public static void compressFilesToStream(OutputStream outputStream, List<ZipUtil.ZipFileEntry> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "압축할 파일이 없습니다");
        }

        int addedFileCount = 0;
        int missingFileCount = 0;

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            // ZIP 압축 레벨 설정 (기본값 사용: 6)
            // zos.setLevel(Deflater.DEFAULT_COMPRESSION);

            for (ZipUtil.ZipFileEntry fileEntry : files) {
                Path sourcePath = fileEntry.sourcePath();

                // 파일 존재 확인
                if (!Files.exists(sourcePath)) {
                    log.warn("파일이 존재하지 않습니다: {} (ZIP 경로: {})",
                            sourcePath, fileEntry.zipEntryPath());
                    missingFileCount++;
                    continue;
                }

                // 디렉토리는 스킵
                if (Files.isDirectory(sourcePath)) {
                    log.debug("디렉토리는 스킵: {}", sourcePath);
                    continue;
                }

                // ZIP 엔트리 생성 (경로 구분자를 슬래시로 통일)
                String entryName = fileEntry.zipEntryPath().replace("\\", "/");
                ZipEntry zipEntry = new ZipEntry(entryName);

                // 파일 메타데이터 설정 (선택사항)
                zipEntry.setTime(Files.getLastModifiedTime(sourcePath).toMillis());
                zipEntry.setSize(Files.size(sourcePath));

                zos.putNextEntry(zipEntry);

                // 파일을 스트리밍 방식으로 복사 (버퍼 사용)
                streamFileTo(sourcePath, zos);

                zos.closeEntry();
                addedFileCount++;

                log.debug("파일 추가: {} -> {} ({} bytes)",
                        sourcePath.getFileName(), entryName, Files.size(sourcePath));
            }

            // 모든 파일이 존재하지 않는 경우 예외 발생
            if (addedFileCount == 0) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "압축할 파일이 실제로 존재하지 않습니다. 누락된 파일 수: " + missingFileCount);
            }

            // 일부 파일만 누락된 경우 경고 로그
            if (missingFileCount > 0) {
                log.warn("일부 파일이 누락되었습니다 ({}/{}개 파일 압축)",
                        addedFileCount, files.size());
            }

            // ZipOutputStream finish 호출 (필수)
            zos.finish();

            log.info("스트리밍 ZIP 압축 완료: {}개 파일 추가 (요청: {}개, 누락: {}개)",
                    addedFileCount, files.size(), missingFileCount);

        } catch (IOException e) {
            log.error("스트리밍 ZIP 압축 실패", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일 압축 실패: " + e.getMessage());
        }
    }

    /**
     * 파일을 버퍼를 사용하여 스트림으로 복사
     *
     * <p>메모리에 전체 파일을 로드하지 않고 8KB 버퍼를 사용하여 스트리밍 복사
     *
     * @param sourcePath 원본 파일 경로
     * @param zos        ZIP 출력 스트림
     * @throws IOException 파일 읽기/쓰기 실패 시
     */
    private static void streamFileTo(Path sourcePath, ZipOutputStream zos) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(
                Files.newInputStream(sourcePath), BUFFER_SIZE)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            // 버퍼 단위로 읽어서 즉시 ZIP 스트림에 쓰기
            while ((bytesRead = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }
    }

    /**
     * 디렉토리 전체를 스트리밍 방식으로 압축
     *
     * @param outputStream 압축된 데이터를 쓸 출력 스트림
     * @param sourceDir    압축할 디렉토리 경로
     * @throws BusinessException 압축 실패 시
     */
    public static void compressDirectoryToStream(OutputStream outputStream, Path sourceDir) {
        if (!Files.exists(sourceDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "압축할 디렉토리를 찾을 수 없습니다: " + sourceDir);
        }

        if (!Files.isDirectory(sourceDir)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "디렉토리가 아닙니다: " + sourceDir);
        }

        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {

            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String entryName = sourceDir.relativize(path)
                                    .toString()
                                    .replace("\\", "/");

                            ZipEntry zipEntry = new ZipEntry(entryName);
                            zipEntry.setTime(Files.getLastModifiedTime(path).toMillis());
                            zipEntry.setSize(Files.size(path));

                            zos.putNextEntry(zipEntry);
                            streamFileTo(path, zos);
                            zos.closeEntry();

                            log.debug("디렉토리 파일 추가: {} -> {}", path.getFileName(), entryName);

                        } catch (IOException e) {
                            throw new RuntimeException("ZIP 압축 중 오류: " + path, e);
                        }
                    });

            zos.finish();
            log.info("디렉토리 스트리밍 압축 완료: {}", sourceDir);

        } catch (IOException e) {
            log.error("디렉토리 압축 실패: {}", sourceDir, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "디렉토리 압축 실패: " + e.getMessage());
        }
    }
}
