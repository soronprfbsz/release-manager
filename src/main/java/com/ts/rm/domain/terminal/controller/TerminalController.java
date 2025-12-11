package com.ts.rm.domain.terminal.controller;

import com.ts.rm.domain.terminal.dto.TerminalDto;
import com.ts.rm.domain.terminal.service.TerminalService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 터미널 REST API Controller
 */
@Tag(name = "터미널", description = "터미널 API")
@Slf4j
@RestController
@RequestMapping("/api/terminal")
@RequiredArgsConstructor
public class TerminalController {

    private final TerminalService shellOrchestrator;

    /**
     * 터미널 생성
     *
     * @param request        연결 요청 정보
     * @param authentication 인증 정보
     * @return 터미널 정보
     */
    @Operation(summary = "터미널 생성", description = "SSH를 통해 터미널 세션을 생성합니다. " +
            "생성 후 WebSocket을 통해 명령어를 전송하고 실시간 출력을 받을 수 있습니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<TerminalDto.ConnectResponse>> createTerminal(
            @Valid @RequestBody TerminalDto.ConnectRequest request,
            Authentication authentication) {

        log.info("터미널 생성 요청: host={}@{}:{}", request.getUsername(), request.getHost(), request.getPort());

        // 현재 사용자 이메일 추출
        String ownerEmail = authentication.getName();

        TerminalDto.ConnectResponse response = shellOrchestrator.connect(request, ownerEmail);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 터미널 정보 조회
     *
     * @param id 터미널 ID
     * @return 터미널 정보
     */
    @Operation(summary = "터미널 정보 조회", description = "특정 터미널의 현재 상태 및 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TerminalDto.ShellSessionInfo>> getTerminal(
            @Parameter(description = "터미널 ID", example = "terminal_2025-12-09T22_00_00_abc123")
            @PathVariable String id) {

        log.info("터미널 정보 조회: id={}", id);

        TerminalDto.ShellSessionInfo response = shellOrchestrator.getSessionInfo(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 터미널 삭제
     *
     * @param id 터미널 ID
     * @return 성공 메시지
     */
    @Operation(summary = "터미널 삭제", description = "활성 터미널을 종료하고 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTerminal(
            @Parameter(description = "터미널 ID", example = "terminal_2025-12-09T22_00_00_abc123")
            @PathVariable String id) {

        log.info("터미널 삭제 요청: id={}", id);

        shellOrchestrator.disconnect(id);

        return ResponseEntity.ok(ApiResponse.success("터미널이 삭제되었습니다"));
    }

    /**
     * 파일 업로드 (클라이언트 → 원격 호스트)
     *
     * @param id     터미널 ID
     * @param file   업로드할 파일
     * @param remotePath 원격 경로 (디렉토리)
     * @return 파일 전송 응답
     */
    @Operation(summary = "파일 업로드", description = "클라이언트에서 원격 SSH 호스트로 파일을 업로드합니다. " +
            "SFTP 프로토콜을 사용하며, 원격 경로가 존재하지 않으면 자동으로 생성됩니다. " +
            "기본 업로드 경로는 /release-manager/uploads 입니다.")
    @PostMapping("/{id}/files")
    public ResponseEntity<ApiResponse<TerminalDto.FileTransferResponse>> uploadFile(
            @Parameter(description = "터미널 ID", example = "terminal_2025-12-09T22_00_00_abc123")
            @PathVariable String id,
            @Parameter(description = "업로드할 파일")
            @RequestPart("file") MultipartFile file,
            @Parameter(description = "원격 경로 (디렉토리, 기본값: /release-manager/uploads)",
                      example = "/release-manager/uploads")
            @RequestParam(value = "remotePath", defaultValue = "/release-manager/uploads") String remotePath) {

        log.info("파일 업로드 요청: terminalId={}, file={}, remotePath={}", id, file.getOriginalFilename(), remotePath);

        TerminalDto.FileTransferResponse response = shellOrchestrator.uploadFile(id, file, remotePath);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 패치 파일 배포 (서버 → 원격 호스트)
     *
     * @param id      터미널 ID
     * @param request 패치 배포 요청
     * @return 파일 전송 응답
     */
    @Operation(summary = "패치 파일 배포", description = "서버에 저장된 패치 파일을 원격 SSH 호스트로 배포합니다. " +
            "SFTP 프로토콜을 사용하며, 원격 경로가 존재하지 않으면 자동으로 생성됩니다. " +
            "기본 배포 경로는 /release-manager/patches 입니다.")
    @PostMapping("/{id}/patches")
    public ResponseEntity<ApiResponse<TerminalDto.FileTransferResponse>> deployPatch(
            @Parameter(description = "터미널 ID", example = "terminal_2025-12-09T22_00_00_abc123")
            @PathVariable String id,
            @Valid @RequestBody TerminalDto.PatchDeploymentRequest request) {

        log.info("패치 파일 배포 요청: terminalId={}, patchId={}, remotePath={}",
                id, request.getPatchId(), request.getRemotePath());

        TerminalDto.FileTransferResponse response = shellOrchestrator.deployPatch(
                id,
                request.getPatchId(),
                request.getRemotePath()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
