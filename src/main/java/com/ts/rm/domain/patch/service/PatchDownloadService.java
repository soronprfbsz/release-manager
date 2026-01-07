package com.ts.rm.domain.patch.service;

import com.ts.rm.domain.patch.dto.PatchDto;
import com.ts.rm.domain.patch.entity.Patch;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.file.StreamingZipUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 패치 다운로드 서비스
 *
 * <p>패치 파일 다운로드, ZIP 스트리밍, 파일 구조 조회, 파일 내용 조회를 담당합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatchDownloadService {

    @Value("${app.release.base-path:src/main/resources/release-manager}")
    private String releaseBasePath;

    /**
     * 패치를 스트리밍 방식으로 ZIP 압축하여 출력 스트림에 작성
     *
     * @param patch        패치 엔티티
     * @param outputStream 출력 스트림
     */
    @Transactional(readOnly = true)
    public void streamPatchAsZip(Patch patch, OutputStream outputStream) {
        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        // 스트리밍 시작 전 파일 존재 여부 검증 (응답 헤더 설정 후 예외 발생 방지)
        validatePatchDirectoryFiles(patchDir);

        StreamingZipUtil.compressDirectoryToStream(outputStream, patchDir);
    }

    /**
     * 패치 ZIP 파일명 생성
     *
     * @param patch 패치 엔티티
     * @return ZIP 파일명 (예: 202511271430_1.0.0_1.1.1.zip)
     */
    public String getZipFileName(Patch patch) {
        return patch.getPatchName() + ".zip";
    }

    /**
     * 패치 디렉토리의 압축 전 총 크기 계산
     *
     * <p>프론트엔드에서 진행률 표시를 위해 사용합니다.
     * 디렉토리 내 모든 파일의 크기를 재귀적으로 계산합니다.
     *
     * @param patch 패치 엔티티
     * @return 압축 전 총 크기 (바이트)
     * @throws BusinessException 디렉토리를 찾을 수 없거나 크기 계산 실패 시
     */
    public long calculateUncompressedSize(Patch patch) {
        log.debug("압축 전 크기 계산 시작 - patchId: {}", patch.getPatchId());

        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        try {
            long totalSize = calculateDirectorySize(patchDir);
            log.info("압축 전 총 크기 계산 완료 - patchId: {}, totalSize: {} bytes ({} MB)",
                    patch.getPatchId(), totalSize, totalSize / (1024.0 * 1024.0));
            return totalSize;
        } catch (IOException e) {
            log.error("디렉토리 크기 계산 실패 - patchId: {}, path: {}", patch.getPatchId(), patchDir, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "디렉토리 크기 계산 실패: " + e.getMessage());
        }
    }

    /**
     * 디렉토리의 총 크기를 재귀적으로 계산
     *
     * @param directory 계산할 디렉토리 경로
     * @return 총 크기 (바이트)
     * @throws IOException 파일 접근 실패 시
     */
    private long calculateDirectorySize(Path directory) throws IOException {
        return Files.walk(directory)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        log.warn("파일 크기 계산 실패 - {}: {}", path, e.getMessage());
                        return 0L;
                    }
                })
                .sum();
    }

    /**
     * 패치 디렉토리 내 파일 존재 여부 검증
     *
     * <p>스트리밍 시작 전에 디렉토리에 최소 1개 이상의 파일이 존재하는지 확인합니다.
     * Content-Type 설정 후 예외 발생을 방지하기 위한 사전 검증입니다.
     *
     * @param directory 패치 디렉토리 경로
     * @throws BusinessException 파일이 하나도 없을 경우
     */
    private void validatePatchDirectoryFiles(Path directory) {
        try {
            long fileCount = Files.walk(directory)
                    .filter(Files::isRegularFile)
                    .count();

            if (fileCount == 0) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "패치 디렉토리에 압축할 파일이 없습니다: " + directory);
            }

            log.debug("패치 파일 검증 완료 - 파일 개수: {}", fileCount);

        } catch (IOException e) {
            log.error("패치 디렉토리 검증 실패: {}", directory, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "패치 디렉토리 검증 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 패치 ZIP 파일 내부 구조 조회
     *
     * @param patch 패치 엔티티
     * @return ZIP 파일 구조 (재귀적인 DirectoryNode)
     */
    @Transactional(readOnly = true)
    public PatchDto.DirectoryNode getZipFileStructure(Patch patch) {
        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        try {
            // 루트 디렉토리 구조 생성
            return buildDirectoryNode(patchDir, patchDir);

        } catch (IOException e) {
            log.error("패치 디렉토리 구조 조회 실패: {}", patchDir, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "패치 디렉토리 구조를 조회할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 디렉토리 구조를 재귀적으로 생성
     *
     * @param directory 조회할 디렉토리
     * @param basePath  기준 경로
     * @return DirectoryNode (재귀 구조)
     */
    private PatchDto.DirectoryNode buildDirectoryNode(Path directory, Path basePath)
            throws IOException {

        String name = directory.getFileName() != null
                ? directory.getFileName().toString()
                : directory.toString();
        String relativePath = basePath.relativize(directory).toString().replace("\\", "/");

        // 빈 경로인 경우 "." 으로 표시
        if (relativePath.isEmpty()) {
            relativePath = ".";
        }

        java.util.List<PatchDto.FileNode> children = new java.util.ArrayList<>();

        try (var stream = Files.list(directory)) {
            stream.sorted().forEach(path -> {
                try {
                    if (Files.isDirectory(path)) {
                        // 하위 디렉토리 재귀 조회
                        PatchDto.DirectoryNode childDir = buildDirectoryNode(path, basePath);

                        // 빈 디렉토리는 제외 (children이 비어있으면 추가하지 않음)
                        if (!childDir.children().isEmpty()) {
                            children.add(childDir);
                        }
                    } else {
                        // 파일 정보 추가
                        children.add(buildFileInfo(path, basePath));
                    }
                } catch (IOException e) {
                    log.warn("파일/디렉토리 정보 조회 실패: {}", path, e);
                }
            });
        }

        return new PatchDto.DirectoryNode(name, "directory", relativePath, children);
    }

    /**
     * 파일 정보 생성
     */
    private PatchDto.FileInfo buildFileInfo(Path filePath, Path basePath) throws IOException {
        String name = filePath.getFileName().toString();
        long size = Files.size(filePath);
        String relativePath = basePath.relativize(filePath).toString().replace("\\", "/");

        return new PatchDto.FileInfo(name, size, "file", relativePath);
    }

    /**
     * 패치 파일 내용 조회
     *
     * @param patch        패치 엔티티
     * @param relativePath 파일 상대 경로 (예: mariadb/source_files/1.1.1/1.patch_mariadb_ddl.sql)
     * @return 파일 내용
     */
    @Transactional(readOnly = true)
    public PatchDto.FileContentResponse getFileContent(Patch patch, String relativePath) {
        // 패치 디렉토리 경로
        Path patchDir = Paths.get(releaseBasePath, patch.getOutputPath());

        if (!Files.exists(patchDir)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "패치 디렉토리를 찾을 수 없습니다: " + patch.getOutputPath());
        }

        // 상대 경로 검증 및 파일 경로 생성
        Path filePath = validateAndResolvePath(patchDir, relativePath);

        // 파일 존재 확인
        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "파일을 찾을 수 없습니다: " + relativePath);
        }

        if (!Files.isRegularFile(filePath)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "디렉토리는 조회할 수 없습니다: " + relativePath);
        }

        try {
            // 파일 크기 확인 (10MB 제한)
            long fileSize = Files.size(filePath);
            if (fileSize > 10 * 1024 * 1024) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "파일 크기가 너무 큽니다 (최대 10MB): " + fileSize + " bytes");
            }

            // 파일 내용 읽기 (UTF-8)
            String content = Files.readString(filePath);

            String fileName = filePath.getFileName().toString();

            return new PatchDto.FileContentResponse(
                    patch.getPatchId(),
                    relativePath,
                    fileName,
                    fileSize,
                    content
            );

        } catch (IOException e) {
            log.error("파일 읽기 실패: {}", filePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일을 읽을 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 경로 검증 및 해석 (경로 탐색 공격 방지)
     *
     * @param baseDir      기준 디렉토리
     * @param relativePath 상대 경로
     * @return 해석된 절대 경로
     * @throws BusinessException 경로가 기준 디렉토리 외부를 가리키는 경우
     */
    private Path validateAndResolvePath(Path baseDir, String relativePath) {
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

        } catch (Exception e) {
            log.error("경로 해석 실패: baseDir={}, relativePath={}",
                    baseDir, relativePath, e);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 파일 경로입니다: " + e.getMessage());
        }
    }
}
