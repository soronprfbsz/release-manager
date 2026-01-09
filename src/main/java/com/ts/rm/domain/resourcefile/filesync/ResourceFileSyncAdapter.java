package com.ts.rm.domain.resourcefile.filesync;

import static com.ts.rm.global.util.FileTypeExtractor.extractFileType;
import static com.ts.rm.global.util.MapExtractUtil.extractString;
import static com.ts.rm.global.util.MapExtractUtil.extractStringOrDefault;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import com.ts.rm.domain.resourcefile.repository.ResourceFileRepository;
import com.ts.rm.global.account.AccountLookupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.domain.filesync.adapter.FileSyncAdapter;
import com.ts.rm.domain.filesync.dto.FileSyncMetadata;
import com.ts.rm.domain.filesync.enums.FileSyncTarget;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 리소스 파일 동기화 어댑터
 *
 * <p>ResourceFile 도메인의 파일 동기화를 담당합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceFileSyncAdapter implements FileSyncAdapter {

    private final ResourceFileRepository resourceFileRepository;
    private final AccountLookupService accountLookupService;

    @Override
    public FileSyncTarget getTarget() {
        return FileSyncTarget.RESOURCE_FILE;
    }

    @Override
    public String getBaseScanPath() {
        return "resources/file";
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileSyncMetadata> getRegisteredFiles(@Nullable String subPath) {
        List<ResourceFile> files;

        if (subPath != null && !subPath.isEmpty()) {
            files = resourceFileRepository.findByFilePathStartingWithOrderByCreatedAtDesc(subPath);
        } else {
            files = resourceFileRepository.findAllByOrderByCreatedAtDesc();
        }

        return files.stream()
                .map(this::toMetadata)
                .toList();
    }

    @Override
    @Transactional
    public Long registerFile(FileSyncMetadata metadata, @Nullable Map<String, Object> additionalData) {
        // 파일 확장자에서 fileType 추출
        String fileType = extractFileType(metadata.getFileName());

        // 경로에서 카테고리/서브카테고리 추출 (resources/file/{category}/{subCategory}/...)
        String[] pathParts = metadata.getFilePath().split("/");
        String fileCategory = pathParts.length > 2 ? pathParts[2].toUpperCase() : "ETC";
        String subCategory = pathParts.length > 3 ? pathParts[3].toUpperCase() : null;

        // additionalData에서 오버라이드 가능
        fileCategory = extractStringOrDefault(additionalData, "fileCategory", fileCategory);
        subCategory = extractStringOrDefault(additionalData, "subCategory", subCategory);
        String resourceFileName = extractStringOrDefault(additionalData, "resourceFileName", metadata.getFileName());
        String description = extractString(additionalData, "description");
        String createdByEmail = extractStringOrDefault(additionalData, "createdByEmail", "SYSTEM_SYNC");

        // sortOrder 자동 채번
        Integer sortOrder = resourceFileRepository.findMaxSortOrderByFileCategory(fileCategory) + 1;

        // 생성자 Account 조회
        Account creator = accountLookupService.findByEmail(createdByEmail);

        ResourceFile resourceFile = ResourceFile.builder()
                .fileType(fileType)
                .fileCategory(fileCategory)
                .subCategory(subCategory)
                .resourceFileName(resourceFileName)
                .fileName(metadata.getFileName())
                .filePath(metadata.getFilePath())
                .fileSize(metadata.getFileSize())
                .checksum(metadata.getChecksum())
                .sortOrder(sortOrder)
                .description(description)
                .creator(creator)
                .createdByEmail(createdByEmail)
                .build();

        ResourceFile saved = resourceFileRepository.save(resourceFile);
        log.info("리소스 파일 동기화 등록: {} (ID: {})", metadata.getFilePath(), saved.getResourceFileId());

        return saved.getResourceFileId();
    }

    @Override
    @Transactional
    public void updateMetadata(Long id, FileSyncMetadata newMetadata) {
        ResourceFile resourceFile = resourceFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND,
                        "리소스 파일을 찾을 수 없습니다: " + id));

        resourceFile.setFileSize(newMetadata.getFileSize());
        resourceFile.setChecksum(newMetadata.getChecksum());

        resourceFileRepository.save(resourceFile);
        log.info("리소스 파일 메타데이터 갱신: {} (ID: {})", newMetadata.getFilePath(), id);
    }

    @Override
    @Transactional
    public void deleteMetadata(Long id) {
        if (!resourceFileRepository.existsById(id)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "리소스 파일을 찾을 수 없습니다: " + id);
        }

        resourceFileRepository.deleteById(id);
        log.info("리소스 파일 메타데이터 삭제: ID {}", id);
    }

    /**
     * ResourceFile 엔티티를 FileSyncMetadata로 변환
     */
    private FileSyncMetadata toMetadata(ResourceFile file) {
        return FileSyncMetadata.builder()
                .id(file.getResourceFileId())
                .filePath(file.getFilePath())
                .fileName(file.getFileName())
                .fileSize(file.getFileSize())
                .checksum(file.getChecksum())
                .registeredAt(file.getCreatedAt())
                .target(FileSyncTarget.RESOURCE_FILE)
                .build();
    }

    /**
     * 유효한 동기화 경로인지 확인
     *
     * <p>리소스 파일은 resources/file/{category}/{subCategory}/{fileName} 형식이어야 합니다.
     * 올바른 경로가 아니면 DB 메타데이터를 생성할 수 없으므로 동기화 대상에서 제외합니다.
     *
     * @param filePath 파일 경로
     * @return true면 동기화 대상, false면 무시
     */
    @Override
    public boolean isValidSyncPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }

        // 경로 형식: resources/file/{category}/{subCategory}/{fileName}
        // 최소 5개의 경로 부분이 필요: [resources, file, category, subCategory, fileName]
        String[] pathParts = filePath.split("/");
        if (pathParts.length < 5) {
            log.debug("리소스 파일 경로 형식 불일치 (최소 5단계 필요): {}", filePath);
            return false;
        }

        // 첫 번째 부분이 "resources"인지 확인
        if (!"resources".equalsIgnoreCase(pathParts[0])) {
            log.debug("리소스 파일 경로는 'resources/'로 시작해야 합니다: {}", filePath);
            return false;
        }

        // 두 번째 부분이 "file"인지 확인
        if (!"file".equalsIgnoreCase(pathParts[1])) {
            log.debug("리소스 파일 경로는 'resources/file/'로 시작해야 합니다: {}", filePath);
            return false;
        }

        // category와 subCategory가 비어있지 않은지 확인
        String category = pathParts[2];
        String subCategory = pathParts[3];
        if (category.isBlank() || subCategory.isBlank()) {
            log.debug("리소스 파일 경로의 category/subCategory가 비어있습니다: {}", filePath);
            return false;
        }

        return true;
    }
}
