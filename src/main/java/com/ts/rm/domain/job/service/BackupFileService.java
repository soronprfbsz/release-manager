package com.ts.rm.domain.job.service;

import com.ts.rm.domain.job.dto.BackupFileDto;
import com.ts.rm.domain.job.entity.BackupFile;
import com.ts.rm.domain.job.mapper.BackupFileDtoMapper;
import com.ts.rm.domain.job.repository.BackupFileRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 백업 파일 서비스
 *
 * <p>백업 파일 조회/다운로드/삭제 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackupFileService {

    private final BackupFileRepository backupFileRepository;
    private final BackupFileDtoMapper backupFileDtoMapper;

    @Value("${app.release.base-path:/app/release_files}")
    private String releaseBasePath;

    /**
     * 백업 파일 다운로드 (스트리밍)
     *
     * @param id           백업 파일 ID
     * @param outputStream 출력 스트림
     */
    public void downloadFile(Long id, OutputStream outputStream) {
        BackupFile backupFile = getBackupFile(id);
        Path filePath = Paths.get(releaseBasePath, backupFile.getFilePath());

        log.info("백업 파일 다운로드 - ID: {}, 경로: {}", id, filePath);

        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND,
                    "파일이 존재하지 않습니다: " + backupFile.getFilePath());
        }

        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (IOException e) {
            log.error("백업 파일 다운로드 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "파일 다운로드에 실패했습니다");
        }
    }

    /**
     * 백업 파일 삭제
     *
     * @param id 백업 파일 ID
     */
    @Transactional
    public void deleteFile(Long id) {
        BackupFile backupFile = getBackupFile(id);
        Path filePath = Paths.get(releaseBasePath, backupFile.getFilePath());

        log.info("백업 파일 삭제 시작 - ID: {}, 경로: {}", id, filePath);

        // 실제 파일 삭제
        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("실제 파일 삭제 완료: {}", filePath);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
            }
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage(), e);
            // 파일 삭제 실패해도 DB 레코드는 삭제 진행
        }

        // DB 레코드 삭제
        backupFileRepository.delete(backupFile);
        log.info("백업 파일 삭제 완료 - ID: {}", id);
    }

    /**
     * 백업 파일 단건 조회
     */
    public BackupFile getBackupFile(Long id) {
        return backupFileRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "백업 파일을 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 백업 파일 목록 조회
     */
    public List<BackupFile> listAllFiles() {
        return backupFileRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * 백업 파일 목록 조회 (검색 + 페이징)
     *
     * @param searchRequest 검색 조건
     * @param pageable      페이징 정보
     * @return 백업 파일 목록
     */
    public Page<BackupFileDto.ListResponse> searchBackupFiles(
            BackupFileDto.SearchRequest searchRequest, Pageable pageable) {

        Page<BackupFile> backupFiles = backupFileRepository.searchBackupFiles(
                searchRequest.fileCategory(),
                searchRequest.fileType(),
                searchRequest.fileName(),
                pageable
        );

        // Page 변환 시 rowNumber 계산
        return backupFiles.map(backupFile -> {
            BackupFileDto.ListResponse response = backupFileDtoMapper.toListResponse(backupFile);
            // rowNumber 계산: 내림차순 정렬이므로 전체에서 역순으로 계산
            long totalElements = backupFiles.getTotalElements();
            int indexInPage = backupFiles.getContent().indexOf(backupFile);
            long rowNumber = totalElements - pageable.getOffset() - indexInPage;

            return new BackupFileDto.ListResponse(
                    rowNumber,
                    response.backupFileId(),
                    response.fileCategory(),
                    response.fileType(),
                    response.fileName(),
                    response.fileSize(),
                    response.fileSizeFormatted(),
                    response.description(),
                    response.createdBy(),
                    response.createdAt()
            );
        });
    }

    /**
     * 파일명 조회
     */
    public String getFileName(Long id) {
        return getBackupFile(id).getFileName();
    }

    /**
     * 파일 크기 조회
     */
    public long getFileSize(Long id) {
        BackupFile backupFile = getBackupFile(id);
        return backupFile.getFileSize() != null ? backupFile.getFileSize() : 0L;
    }
}
