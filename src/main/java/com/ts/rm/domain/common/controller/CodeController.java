package com.ts.rm.domain.common.controller;

import com.ts.rm.domain.common.dto.CodeDto;
import com.ts.rm.domain.common.service.CodeService;
import com.ts.rm.global.response.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공통 코드 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController implements CodeControllerDocs {

    private final CodeService codeService;

    @Override
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<CodeDto.CodeTypeResponse>>> getCodeTypes() {
        log.info("GET /api/codes/types - 코드 타입 목록 조회 요청");

        List<CodeDto.CodeTypeResponse> response = codeService.getCodeTypes();

        log.info("GET /api/codes/types - 코드 타입 목록 조회 완료: {}개", response.size());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<List<CodeDto.SimpleResponse>>> getCodesByType(@PathVariable String id) {
        log.info("GET /api/codes/{} - 코드 타입별 코드 목록 조회 요청", id);

        List<CodeDto.SimpleResponse> response = codeService.getCodesByType(id);

        log.info("GET /api/codes/{} - 코드 타입별 코드 목록 조회 완료: {}개", id, response.size());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
