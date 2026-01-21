package com.ts.rm.domain.releaseversion.controller;

import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionService;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionTreeService;
import com.ts.rm.domain.releaseversion.service.ReleaseVersionUploadService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ReleaseVersion Controller
 *
 * <p>릴리즈 버전 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleaseVersionController implements ReleaseVersionControllerDocs {

    private final ReleaseVersionService releaseVersionService;
    private final ReleaseVersionUploadService uploadService;
    private final ReleaseVersionTreeService treeService;

    /**
     * 표준 릴리즈 버전 생성 (ZIP 파일 업로드)
     *
     * @param request       버전 생성 요청 (version, comment)
     * @param patchFiles    패치 파일 ZIP
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 생성된 버전 정보
     */
    @Override
    @PostMapping(value = "/versions/standard", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReleaseVersionDto.CreateVersionResponse>> createStandardVersion(
            @Valid @ModelAttribute ReleaseVersionDto.CreateStandardVersionRequest request,
            @RequestPart("patchFiles") MultipartFile patchFiles,
            @RequestHeader("Authorization") String authorization) {

        log.info("표준 릴리즈 버전 생성 요청 - projectId: {}, version: {}, comment: {}, fileSize: {}",
                request.projectId(), request.version(), request.comment(), patchFiles.getSize());

        // SecurityUtil에서 현재 인증된 사용자 정보 추출
        String createdBy = SecurityUtil.getTokenInfo().email();

        log.info("버전 생성자: {}", createdBy);

        // 버전 생성
        ReleaseVersionDto.CreateVersionResponse response = uploadService.createStandardVersionWithZip(
                request.projectId(),
                request.version(),
                request.comment(),
                patchFiles,
                createdBy,
                request.isApproved()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 커스텀 릴리즈 버전 생성 (ZIP 파일 업로드)
     *
     * @param request       버전 생성 요청 (customerId, customBaseVersionId, customVersion, comment)
     * @param patchFiles    패치 파일 ZIP
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 생성된 버전 정보
     */
    @Override
    @PostMapping(value = "/versions/custom", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReleaseVersionDto.CreateCustomVersionResponse>> createCustomVersion(
            @Valid @ModelAttribute ReleaseVersionDto.CreateCustomVersionRequest request,
            @RequestPart("patchFiles") MultipartFile patchFiles,
            @RequestHeader("Authorization") String authorization) {

        log.info("커스텀 릴리즈 버전 생성 요청 - projectId: {}, customerId: {}, customBaseVersionId: {}, customVersion: {}, comment: {}, fileSize: {}",
                request.projectId(), request.customerId(), request.customBaseVersionId(),
                request.customVersion(), request.comment(), patchFiles.getSize());

        // SecurityUtil에서 현재 인증된 사용자 정보 추출
        String createdBy = SecurityUtil.getTokenInfo().email();

        log.info("버전 생성자: {}", createdBy);

        // 커스텀 버전 생성
        ReleaseVersionDto.CreateCustomVersionResponse response = uploadService.createCustomVersionWithZip(
                request,
                patchFiles,
                createdBy
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 조회 (ID)
     *
     * @param id 버전 ID
     * @return 버전 상세 정보
     */
    @Override
    @GetMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> getVersionById(@PathVariable Long id) {
        ReleaseVersionDto.DetailResponse response = releaseVersionService.getVersionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 표준 릴리즈 버전 트리 조회 (프로젝트별)
     *
     * @param id 프로젝트 ID
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Override
    @GetMapping("/projects/{id}/standard/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getStandardReleaseTree(@PathVariable String id) {
        ReleaseVersionDto.TreeResponse response = treeService.getStandardReleaseTree(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 전체 커스텀 릴리즈 버전 트리 조회 (프로젝트별, 모든 고객사)
     *
     * @param id 프로젝트 ID
     * @return 커스텀 릴리즈 버전 트리 (고객사별 그룹화)
     */
    @Override
    @GetMapping("/projects/{id}/custom/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.CustomTreeResponse>> getAllCustomReleaseTree(
            @PathVariable String id) {
        ReleaseVersionDto.CustomTreeResponse response = treeService.getAllCustomReleaseTree(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 커스텀 릴리즈 버전 트리 조회 (프로젝트별, 특정 고객사)
     *
     * @param id           프로젝트 ID
     * @param customerCode 고객사 코드
     * @return 릴리즈 버전 트리 (계층 구조)
     */
    @Override
    @GetMapping("/projects/{id}/custom/{customer-code}/tree")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.TreeResponse>> getCustomReleaseTree(
            @PathVariable String id,
            @PathVariable("customer-code") String customerCode) {
        ReleaseVersionDto.TreeResponse response = treeService.getCustomReleaseTree(id, customerCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 삭제
     *
     * @param id 버전 ID
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/versions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(@PathVariable Long id) {

        log.info("릴리즈 버전 삭제 요청 - ID: {}", id);

        releaseVersionService.deleteVersion(id);

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 릴리즈 버전 파일 트리 조회
     *
     * @param id 버전 ID
     * @return 파일 트리 응답
     */
    @Override
    @GetMapping("/versions/{id}/files")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.FileTreeResponse>> getVersionFileTree(@PathVariable Long id) {

        log.info("릴리즈 버전 파일 트리 조회 요청 - ID: {}", id);

        // 버전 조회
        com.ts.rm.domain.releaseversion.entity.ReleaseVersion version = releaseVersionService.findVersionById(id);

        ReleaseVersionDto.FileTreeResponse response = treeService.getVersionFileTree(id, version);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로젝트별 표준본 버전 목록 조회 (셀렉트박스용)
     *
     * @param id 프로젝트 ID
     * @return 표준본 버전 목록
     */
    @Override
    @GetMapping("/projects/{id}/versions")
    public ResponseEntity<ApiResponse<java.util.List<ReleaseVersionDto.VersionSelectOption>>> getStandardVersionsForSelect(
            @PathVariable String id) {

        log.info("표준본 버전 셀렉트박스 목록 조회 요청 - projectId: {}", id);

        java.util.List<ReleaseVersionDto.VersionSelectOption> response = releaseVersionService.getStandardVersionsForSelect(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 코멘트 수정
     *
     * @param id      버전 ID
     * @param request 수정 요청 (comment)
     * @return 수정된 버전 상세 정보
     */
    @Override
    @PatchMapping("/versions/{id}/comment")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody ReleaseVersionDto.UpdateRequest request) {

        log.info("릴리즈 버전 코멘트 수정 요청 - ID: {}, comment: {}", id, request.comment());

        ReleaseVersionDto.DetailResponse response = releaseVersionService.updateVersion(id, request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 릴리즈 버전 승인
     *
     * @param id            버전 ID
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 승인된 버전 상세 정보
     */
    @Override
    @PatchMapping("/versions/{id}/approve")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.DetailResponse>> approveVersion(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authorization) {

        log.info("릴리즈 버전 승인 요청 - ID: {}", id);

        // SecurityUtil에서 현재 인증된 사용자 정보 추출
        String approvedBy = SecurityUtil.getTokenInfo().email();

        log.info("승인자: {}", approvedBy);

        ReleaseVersionDto.DetailResponse response = releaseVersionService.approveReleaseVersion(id, approvedBy);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========================================
    // Hotfix API
    // ========================================

    /**
     * 핫픽스 생성 (ZIP 파일 업로드)
     *
     * @param id            원본 버전 ID
     * @param request       핫픽스 생성 요청 (comment)
     * @param patchFiles    패치 파일 ZIP
     * @param authorization JWT 토큰 (Bearer {token})
     * @return 생성된 핫픽스 정보
     */
    @Override
    @PostMapping(value = "/versions/{id}/hotfix", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReleaseVersionDto.CreateHotfixResponse>> createHotfix(
            @PathVariable Long id,
            @Valid @ModelAttribute ReleaseVersionDto.CreateHotfixRequest request,
            @RequestPart("patchFiles") MultipartFile patchFiles,
            @RequestHeader("Authorization") String authorization) {

        log.info("핫픽스 생성 요청 - hotfixBaseVersionId: {}, comment: {}, fileSize: {}",
                id, request.comment(), patchFiles.getSize());

        // SecurityUtil에서 현재 인증된 사용자 정보 추출
        String createdBy = SecurityUtil.getTokenInfo().email();

        log.info("핫픽스 생성자: {}", createdBy);

        // 핫픽스 생성
        ReleaseVersionDto.CreateHotfixResponse response = uploadService.createHotfixWithZip(
                id,
                request.comment(),
                patchFiles,
                createdBy,
                request.engineerId(),
                request.isApprovedOrDefault()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 버전의 핫픽스 목록 조회
     *
     * @param id 원본 버전 ID
     * @return 핫픽스 목록
     */
    @Override
    @GetMapping("/versions/{id}/hotfixes")
    public ResponseEntity<ApiResponse<ReleaseVersionDto.HotfixListResponse>> getHotfixesByVersionId(
            @PathVariable Long id) {

        log.info("핫픽스 목록 조회 요청 - hotfixBaseVersionId: {}", id);

        ReleaseVersionDto.HotfixListResponse response = releaseVersionService.getHotfixesByHotfixBaseVersionId(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
