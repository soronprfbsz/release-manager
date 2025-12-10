package com.ts.rm.domain.shell.controller;

import com.ts.rm.domain.shell.dto.InteractiveShellDto;
import com.ts.rm.domain.shell.service.InteractiveShellOrchestrator;
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

/**
 * 대화형 SSH 셸 REST API Controller
 */
@Tag(name = "Interactive SSH Shell", description = "대화형 SSH 터미널 API")
@Slf4j
@RestController
@RequestMapping("/api/shell")
@RequiredArgsConstructor
public class InteractiveShellController {

    private final InteractiveShellOrchestrator shellOrchestrator;

    /**
     * 셸 연결
     *
     * @param request        연결 요청 정보
     * @param authentication 인증 정보
     * @return 세션 정보
     */
    @Operation(summary = "셸 연결", description = "SSH를 통해 대화형 터미널 세션을 연결합니다. " +
            "연결 후 WebSocket을 통해 명령어를 전송하고 실시간 출력을 받을 수 있습니다.")
    @PostMapping("/connect")
    public ResponseEntity<ApiResponse<InteractiveShellDto.ConnectResponse>> connect(
            @Valid @RequestBody InteractiveShellDto.ConnectRequest request,
            Authentication authentication) {

        log.info("셸 연결 요청: host={}@{}:{}", request.getUsername(), request.getHost(), request.getPort());

        // 현재 사용자 이메일 추출
        String ownerEmail = authentication.getName();

        InteractiveShellDto.ConnectResponse response = shellOrchestrator.connect(request, ownerEmail);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 세션 정보 조회
     *
     * @param shellSessionId 셸 세션 ID
     * @return 세션 정보
     */
    @Operation(summary = "세션 정보 조회", description = "특정 셸 세션의 현재 상태 및 정보를 조회합니다.")
    @GetMapping("/sessions/{shellSessionId}")
    public ResponseEntity<ApiResponse<InteractiveShellDto.ShellSessionInfo>> getSessionInfo(
            @Parameter(description = "셸 세션 ID", example = "shell_2025-12-09T22_00_00_abc123")
            @PathVariable String shellSessionId) {

        log.info("셸 세션 정보 조회: shellSessionId={}", shellSessionId);

        InteractiveShellDto.ShellSessionInfo response = shellOrchestrator.getSessionInfo(shellSessionId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 셸 연결 종료
     *
     * @param shellSessionId 셸 세션 ID
     * @return 성공 메시지
     */
    @Operation(summary = "셸 연결 종료", description = "활성 셸 세션을 종료합니다.")
    @DeleteMapping("/sessions/{shellSessionId}")
    public ResponseEntity<ApiResponse<String>> disconnect(
            @Parameter(description = "셸 세션 ID", example = "shell_2025-12-09T22_00_00_abc123")
            @PathVariable String shellSessionId) {

        log.info("셸 연결 종료 요청: shellSessionId={}", shellSessionId);

        shellOrchestrator.disconnect(shellSessionId);

        return ResponseEntity.ok(ApiResponse.success("셸 연결이 종료되었습니다"));
    }
}
