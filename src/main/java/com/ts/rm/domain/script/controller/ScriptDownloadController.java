package com.ts.rm.domain.script.controller;

import com.ts.rm.domain.script.enums.ScriptType;
import com.ts.rm.domain.script.service.ScriptDownloadService;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 스크립트 다운로드 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/scripts")
@RequiredArgsConstructor
@Tag(name = "스크립트", description = "DB 백업/복원 스크립트 다운로드 API")
public class ScriptDownloadController {

    private final ScriptDownloadService scriptDownloadService;

    /**
     * 스크립트 타입 목록 조회
     */
    @GetMapping("/types")
    @Operation(summary = "스크립트 타입 목록 조회", description = "다운로드 가능한 스크립트 타입 목록을 조회합니다.")
    public ApiResponse<List<Map<String, String>>> getScriptTypes() {
        log.info("스크립트 타입 목록 조회 요청");
        List<Map<String, String>> types = scriptDownloadService.getScriptTypes();
        return ApiResponse.success(types);
    }

    /**
     * 스크립트 파일 다운로드
     */
    @GetMapping("/download")
    @Operation(summary = "스크립트 다운로드",
            description = "지정한 타입의 스크립트 파일을 다운로드합니다.\n\n"
                    + "**사용 가능한 타입:**\n"
                    + "- `mariadb-backup`: MariaDB 백업 스크립트\n"
                    + "- `mariadb-restore`: MariaDB 복원 스크립트\n"
                    + "- `cratedb-backup`: CrateDB 백업 스크립트\n"
                    + "- `cratedb-restore`: CrateDB 복원 스크립트")
    public ResponseEntity<Resource> downloadScript(
            @Parameter(description = "스크립트 타입 (mariadb-backup, mariadb-restore, cratedb-backup, cratedb-restore)",
                    example = "mariadb-backup")
            @RequestParam String type) {

        log.info("스크립트 다운로드 요청 - type: {}", type);

        ScriptType scriptType = ScriptType.fromCode(type);
        Resource resource = scriptDownloadService.getScriptResource(scriptType);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + scriptType.getFileName() + "\"")
                .body(resource);
    }
}
