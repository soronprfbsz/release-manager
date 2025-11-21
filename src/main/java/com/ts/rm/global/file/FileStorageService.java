package com.ts.rm.global.file;

import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장소 서비스
 *
 * <p>릴리즈 파일의 물리적 저장 및 관리를 담당
 */
@Slf4j
@Service
public class FileStorageService {

    private final Path baseStorageLocation;

    public FileStorageService(
            @Value("${app.release.base-path:src/main/resources/release}") String basePath) {
        this.baseStorageLocation = Paths.get(basePath).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.baseStorageLocation);
            log.info("파일 저장소 초기화 완료: {}", this.baseStorageLocation);
        } catch (IOException e) {
            log.error("파일 저장소 초기화 실패 - 경로: {}, 오류: {}",
                    this.baseStorageLocation, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    String.format("파일 저장소를 초기화할 수 없습니다: %s (경로: %s)",
                            e.getMessage(), this.baseStorageLocation));
        }
    }

    /**
     * 파일 저장
     *
     * @param file         업로드 파일
     * @param relativePath 상대 경로 (예: releases/standard/1.1.x/1.1.3/patch/mariadb/001.sql)
     * @return 저장된 파일의 상대 경로
     */
    public String saveFile(MultipartFile file, String relativePath) {
        try {
            // 파일명 검증
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.contains("..")) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "유효하지 않은 파일명입니다: " + fileName);
            }

            // 대상 경로 생성
            Path targetLocation = this.baseStorageLocation.resolve(relativePath);

            // 상위 디렉토리 생성
            Files.createDirectories(targetLocation.getParent());

            // 파일 저장
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {}", relativePath);
            return relativePath;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", relativePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일 저장에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 로드
     *
     * @param relativePath 상대 경로
     * @return Resource
     */
    public Resource loadFile(String relativePath) {
        try {
            Path filePath = this.baseStorageLocation.resolve(relativePath).normalize();

            if (!filePath.startsWith(this.baseStorageLocation)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "허용되지 않은 경로입니다: " + relativePath);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                log.debug("파일 로드 성공: {}", relativePath);
                return resource;
            } else {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "파일을 찾을 수 없습니다: " + relativePath);
            }

        } catch (IOException e) {
            log.error("파일 로드 실패: {}", relativePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일을 로드할 수 없습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 삭제
     *
     * @param relativePath 상대 경로
     */
    public void deleteFile(String relativePath) {
        try {
            Path filePath = this.baseStorageLocation.resolve(relativePath).normalize();

            if (!filePath.startsWith(this.baseStorageLocation)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "허용되지 않은 경로입니다: " + relativePath);
            }

            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", relativePath);

        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", relativePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 복사
     *
     * @param sourceRelativePath 원본 상대 경로
     * @param targetRelativePath 대상 상대 경로
     */
    public void copyFile(String sourceRelativePath, String targetRelativePath) {
        try {
            Path sourcePath = this.baseStorageLocation.resolve(sourceRelativePath).normalize();
            Path targetPath = this.baseStorageLocation.resolve(targetRelativePath).normalize();

            // 경로 검증
            if (!sourcePath.startsWith(this.baseStorageLocation) ||
                    !targetPath.startsWith(this.baseStorageLocation)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "허용되지 않은 경로입니다");
            }

            // 원본 파일 존재 확인
            if (!Files.exists(sourcePath)) {
                throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "원본 파일을 찾을 수 없습니다: " + sourceRelativePath);
            }

            // 대상 디렉토리 생성
            Files.createDirectories(targetPath.getParent());

            // 파일 복사
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("파일 복사 완료: {} -> {}", sourceRelativePath, targetRelativePath);

        } catch (IOException e) {
            log.error("파일 복사 실패: {} -> {}", sourceRelativePath, targetRelativePath, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "파일 복사에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 존재 여부 확인
     *
     * @param relativePath 상대 경로
     * @return 존재 여부
     */
    public boolean fileExists(String relativePath) {
        Path filePath = this.baseStorageLocation.resolve(relativePath).normalize();
        return Files.exists(filePath);
    }

    /**
     * 절대 경로 반환
     *
     * @param relativePath 상대 경로
     * @return 절대 경로
     */
    public Path getAbsolutePath(String relativePath) {
        return this.baseStorageLocation.resolve(relativePath).normalize();
    }
}
