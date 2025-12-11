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
}
