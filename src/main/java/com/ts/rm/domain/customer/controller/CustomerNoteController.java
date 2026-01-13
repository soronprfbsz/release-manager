package com.ts.rm.domain.customer.controller;

import com.ts.rm.domain.customer.dto.CustomerNoteDto;
import com.ts.rm.domain.customer.service.CustomerNoteService;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.security.SecurityUtil;
import com.ts.rm.global.security.TokenInfo;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CustomerNote Controller
 *
 * <p>고객사 특이사항 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/customers/{customerId}/notes")
@RequiredArgsConstructor
public class CustomerNoteController implements CustomerNoteControllerDocs {

    private final CustomerNoteService customerNoteService;

    /**
     * 특이사항 목록 조회
     *
     * @param customerId 고객사 ID
     * @return 특이사항 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerNoteDto.Response>>> getNotes(
            @PathVariable Long customerId) {

        log.info("특이사항 목록 조회 요청 - customerId: {}", customerId);

        List<CustomerNoteDto.Response> response = customerNoteService.getNotes(customerId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특이사항 생성
     *
     * @param customerId 고객사 ID
     * @param request    생성 요청
     * @return 생성된 특이사항
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerNoteDto.Response>> createNote(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerNoteDto.CreateRequest request) {

        log.info("특이사항 생성 요청 - customerId: {}, title: {}", customerId, request.title());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        CustomerNoteDto.Response response = customerNoteService.createNote(
                customerId, request, tokenInfo.email());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 특이사항 수정
     *
     * <p>ADMIN 역할이거나 작성자만 수정 가능
     *
     * @param customerId 고객사 ID
     * @param noteId     특이사항 ID
     * @param request    수정 요청
     * @return 수정된 특이사항
     */
    @Override
    @PutMapping("/{noteId}")
    public ResponseEntity<ApiResponse<CustomerNoteDto.Response>> updateNote(
            @PathVariable Long customerId,
            @PathVariable Long noteId,
            @Valid @RequestBody CustomerNoteDto.UpdateRequest request) {

        log.info("특이사항 수정 요청 - customerId: {}, noteId: {}", customerId, noteId);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        CustomerNoteDto.Response response = customerNoteService.updateNote(
                customerId, noteId, request, tokenInfo.email(), tokenInfo.role());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특이사항 삭제
     *
     * <p>ADMIN 역할이거나 작성자만 삭제 가능
     *
     * @param customerId 고객사 ID
     * @param noteId     특이사항 ID
     * @return 성공 응답
     */
    @Override
    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long customerId,
            @PathVariable Long noteId) {

        log.info("특이사항 삭제 요청 - customerId: {}, noteId: {}", customerId, noteId);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();

        customerNoteService.deleteNote(customerId, noteId, tokenInfo.email(), tokenInfo.role());

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
