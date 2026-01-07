package com.ts.rm.global.file;

import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 내용 조회 공통 유틸리티
 *
 * <p>파일 경로 검증, MIME 타입 판단, 텍스트/바이너리 분기 처리를 제공합니다.
 *
 * <p>사용 예시:
 * <pre>{@code
 * Path filePath = FileContentUtil.validateAndResolvePath(baseDir, "css/style.css");
 * FileContentResult result = FileContentUtil.readFileContent(filePath, 10 * 1024 * 1024);
 *
 * return new FileContentResponse(
 *     id,
 *     relativePath,
 *     result.fileName(),
 *     result.size(),
 *     result.mimeType(),
 *     result.isBinary(),
 *     result.content()
 * );
 * }</pre>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileContentUtil {

    /** 기본 최대 파일 크기 (10MB) */
    public static final long DEFAULT_MAX_SIZE_BYTES = 10 * 1024 * 1024;

    /**
     * 파일 내용 조회 결과
     *
     * @param fileName 파일명
     * @param size     파일 크기 (bytes)
     * @param mimeType MIME 타입
     * @param isBinary 바이너리 파일 여부 (true면 content는 Base64 인코딩됨)
     * @param content  파일 내용 (텍스트 또는 Base64)
     */
    public record FileContentResult(
            String fileName,
            long size,
            String mimeType,
            boolean isBinary,
            String content
    ) {
    }

    /**
     * 파일 내용 조회 (기본 최대 크기: 10MB)
     *
     * @param filePath 파일 경로
     * @return 파일 내용 결과
     * @throws BusinessException 파일이 존재하지 않거나 읽기 실패 시
     */
    public static FileContentResult readFileContent(Path filePath) {
        return readFileContent(filePath, DEFAULT_MAX_SIZE_BYTES);
    }

    /**
     * 파일 내용 조회
     *
     * <p>텍스트 파일은 UTF-8 문자열로, 바이너리 파일은 Base64 인코딩하여 반환합니다.
     *
     * @param filePath     파일 경로
     * @param maxSizeBytes 최대 허용 파일 크기 (bytes)
     * @return 파일 내용 결과
     * @throws BusinessException 파일이 존재하지 않거나, 크기 초과 또는 읽기 실패 시
     */
    public static FileContentResult readFileContent(Path filePath, long maxSizeBytes) {
        // 파일 존재 확인
        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "파일을 찾을 수 없습니다: " + filePath.getFileName());
        }

        if (!Files.isRegularFile(filePath)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "디렉토리는 조회할 수 없습니다: " + filePath.getFileName());
        }

        try {
            // 파일 크기 확인
            long fileSize = Files.size(filePath);
            if (fileSize > maxSizeBytes) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        String.format("파일 크기가 너무 큽니다 (최대 %dMB): %d bytes",
                                maxSizeBytes / (1024 * 1024), fileSize));
            }

            String fileName = filePath.getFileName().toString();

            // MIME 타입 확인
            String mimeType = Files.probeContentType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            // 텍스트 파일 여부 판단
            boolean isBinary = !isTextMimeType(mimeType);

            String content;
            if (isBinary) {
                // 바이너리 파일: Base64 인코딩
                byte[] bytes = Files.readAllBytes(filePath);
                content = Base64.getEncoder().encodeToString(bytes);
            } else {
                // 텍스트 파일: UTF-8로 읽기
                content = Files.readString(filePath);
            }

            return new FileContentResult(fileName, fileSize, mimeType, isBinary, content);

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", filePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일을 읽을 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 경로 검증 및 해석 (경로 탐색 공격 방지)
     *
     * <p>상대 경로가 기준 디렉토리 외부를 가리키는 경우 예외를 발생시킵니다.
     *
     * @param baseDir      기준 디렉토리
     * @param relativePath 상대 경로
     * @return 해석된 절대 경로
     * @throws BusinessException 경로가 비어있거나 기준 디렉토리 외부를 가리키는 경우
     */
    public static Path validateAndResolvePath(Path baseDir, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "파일 경로가 비어있습니다");
        }

        try {
            // 상대 경로를 절대 경로로 변환
            Path resolvedPath = baseDir.resolve(relativePath).normalize();

            // 경로 탐색 공격 방지: 해석된 경로가 기준 디렉토리 내부에 있는지 확인
            if (!resolvedPath.startsWith(baseDir)) {
                log.warn("경로 탐색 공격 시도 감지: baseDir={}, relativePath={}, resolved={}",
                        baseDir, relativePath, resolvedPath);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "유효하지 않은 파일 경로입니다");
            }

            return resolvedPath;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("경로 해석 실패: baseDir={}, relativePath={}", baseDir, relativePath, e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 파일 경로입니다: " + e.getMessage());
        }
    }

    /**
     * MIME 타입이 텍스트 파일인지 확인
     *
     * @param mimeType MIME 타입
     * @return 텍스트 파일 여부
     */
    public static boolean isTextMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }

        // 텍스트 계열 MIME 타입
        if (mimeType.startsWith("text/")) {
            return true;
        }

        // 텍스트로 취급할 application/* 타입
        return mimeType.equals("application/json") ||
               mimeType.equals("application/xml") ||
               mimeType.equals("application/javascript") ||
               mimeType.equals("application/x-javascript") ||
               mimeType.equals("application/ecmascript") ||
               mimeType.equals("application/xhtml+xml") ||
               mimeType.equals("application/x-sh") ||
               mimeType.equals("application/x-httpd-php") ||
               mimeType.equals("application/sql") ||
               mimeType.equals("application/graphql") ||
               mimeType.equals("application/ld+json") ||
               mimeType.equals("application/manifest+json") ||
               mimeType.equals("application/x-yaml") ||
               mimeType.equals("application/yaml") ||
               mimeType.endsWith("+xml") ||
               mimeType.endsWith("+json");
    }
}
