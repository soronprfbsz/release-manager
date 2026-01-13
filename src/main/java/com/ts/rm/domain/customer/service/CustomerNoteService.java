package com.ts.rm.domain.customer.service;

import com.ts.rm.domain.account.entity.Account;
import com.ts.rm.domain.customer.dto.CustomerNoteDto;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.entity.CustomerNote;
import com.ts.rm.domain.customer.mapper.CustomerNoteDtoMapper;
import com.ts.rm.domain.customer.repository.CustomerNoteRepository;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.global.account.AccountLookupService;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CustomerNote Service
 *
 * <p>고객사 특이사항 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerNoteService {

    private final CustomerNoteRepository customerNoteRepository;
    private final CustomerRepository customerRepository;
    private final CustomerNoteDtoMapper mapper;
    private final AccountLookupService accountLookupService;

    /**
     * 고객사 특이사항 목록 조회
     *
     * @param customerId 고객사 ID
     * @return 특이사항 목록 (최신순)
     */
    public List<CustomerNoteDto.Response> getNotes(Long customerId) {
        log.debug("특이사항 목록 조회 - customerId: {}", customerId);

        // 고객사 존재 확인
        validateCustomerExists(customerId);

        List<CustomerNote> notes = customerNoteRepository.findAllByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);
        return mapper.toResponseList(notes);
    }

    /**
     * 고객사 특이사항 생성
     *
     * @param customerId     고객사 ID
     * @param request        생성 요청
     * @param createdByEmail 생성자 이메일
     * @return 생성된 특이사항
     */
    @Transactional
    public CustomerNoteDto.Response createNote(Long customerId, CustomerNoteDto.CreateRequest request,
            String createdByEmail) {
        log.info("특이사항 생성 - customerId: {}, title: {}", customerId, request.title());

        // 고객사 존재 확인
        Customer customer = findCustomerById(customerId);

        // 생성자 조회
        Account creator = accountLookupService.findByEmail(createdByEmail);

        // 엔티티 생성 및 저장
        CustomerNote note = mapper.toEntity(request);
        note.setCustomer(customer);
        note.setCreator(creator);
        note.setCreatedByEmail(creator.getEmail());
        note.setUpdater(creator);
        note.setUpdatedByEmail(creator.getEmail());

        CustomerNote savedNote = customerNoteRepository.save(note);

        log.info("특이사항 생성 완료 - noteId: {}", savedNote.getNoteId());
        return mapper.toResponse(savedNote);
    }

    /**
     * 고객사 특이사항 수정
     *
     * <p>ADMIN 역할이거나 작성자만 수정 가능
     *
     * @param customerId     고객사 ID
     * @param noteId         특이사항 ID
     * @param request        수정 요청
     * @param updatedByEmail 수정자 이메일
     * @param role           수정자 역할
     * @return 수정된 특이사항
     */
    @Transactional
    public CustomerNoteDto.Response updateNote(Long customerId, Long noteId,
            CustomerNoteDto.UpdateRequest request, String updatedByEmail, String role) {
        log.info("특이사항 수정 - customerId: {}, noteId: {}", customerId, noteId);

        // 특이사항 조회 및 고객사 일치 검증
        CustomerNote note = findNoteByIdAndCustomerId(noteId, customerId);

        // 권한 검증: ADMIN이거나 작성자만 수정 가능
        validateModifyPermission(note, updatedByEmail, role);

        // 수정자 조회
        Account updater = accountLookupService.findByEmail(updatedByEmail);

        // 수정 (null이 아닌 필드만)
        if (request.title() != null) {
            note.setTitle(request.title());
        }
        if (request.content() != null) {
            note.setContent(request.content());
        }
        note.setUpdater(updater);
        note.setUpdatedByEmail(updater.getEmail());

        log.info("특이사항 수정 완료 - noteId: {}", noteId);
        return mapper.toResponse(note);
    }

    /**
     * 고객사 특이사항 삭제
     *
     * <p>ADMIN 역할이거나 작성자만 삭제 가능
     *
     * @param customerId    고객사 ID
     * @param noteId        특이사항 ID
     * @param requestEmail  요청자 이메일
     * @param role          요청자 역할
     */
    @Transactional
    public void deleteNote(Long customerId, Long noteId, String requestEmail, String role) {
        log.info("특이사항 삭제 - customerId: {}, noteId: {}", customerId, noteId);

        // 특이사항 조회 및 고객사 일치 검증
        CustomerNote note = findNoteByIdAndCustomerId(noteId, customerId);

        // 권한 검증: ADMIN이거나 작성자만 삭제 가능
        validateModifyPermission(note, requestEmail, role);

        customerNoteRepository.delete(note);

        log.info("특이사항 삭제 완료 - noteId: {}", noteId);
    }

    // === Private Helper Methods ===

    private static final String ROLE_ADMIN = "ADMIN";

    /**
     * 수정/삭제 권한 검증
     *
     * <p>ADMIN 역할이거나 작성자만 수정/삭제 가능
     *
     * @param note         특이사항 엔티티
     * @param requestEmail 요청자 이메일
     * @param role         요청자 역할
     */
    private void validateModifyPermission(CustomerNote note, String requestEmail, String role) {
        // ADMIN은 모든 특이사항 수정/삭제 가능
        if (ROLE_ADMIN.equals(role)) {
            return;
        }

        // 작성자 본인만 수정/삭제 가능
        if (note.getCreatedByEmail() != null && note.getCreatedByEmail().equals(requestEmail)) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN,
                "특이사항 수정/삭제 권한이 없습니다. ADMIN 또는 작성자만 가능합니다.");
    }

    /**
     * 고객사 존재 확인
     */
    private void validateCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND);
        }
    }

    /**
     * 고객사 조회
     */
    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }

    /**
     * 특이사항 조회 및 고객사 일치 검증
     */
    private CustomerNote findNoteByIdAndCustomerId(Long noteId, Long customerId) {
        CustomerNote note = customerNoteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,
                        "특이사항을 찾을 수 없습니다: " + noteId));

        // 고객사 ID 일치 검증
        if (!note.getCustomer().getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "해당 고객사의 특이사항이 아닙니다: noteId=" + noteId + ", customerId=" + customerId);
        }

        return note;
    }
}
