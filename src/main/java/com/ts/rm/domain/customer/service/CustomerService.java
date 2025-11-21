package com.ts.rm.domain.customer.service;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.mapper.CustomerDtoMapper;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Customer Service
 *
 * <p>고객사 관리 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerDtoMapper mapper;

    /**
     * 고객사 생성
     *
     * @param request 고객사 생성 요청
     * @return 생성된 고객사 상세 정보
     */
    @Transactional
    public CustomerDto.DetailResponse createCustomer(CustomerDto.CreateRequest request) {
        log.info("Creating customer with code: {}", request.customerCode());

        // 중복 검증
        if (customerRepository.existsByCustomerCode(request.customerCode())) {
            throw new BusinessException(ErrorCode.CUSTOMER_CODE_CONFLICT);
        }

        Customer customer = mapper.toEntity(request);
        Customer savedCustomer = customerRepository.save(customer);

        log.info("Customer created successfully with id: {}", savedCustomer.getCustomerId());
        return mapper.toDetailResponse(savedCustomer);
    }

    /**
     * 고객사 조회 (ID)
     *
     * @param customerId 고객사 ID
     * @return 고객사 상세 정보
     */
    public CustomerDto.DetailResponse getCustomerById(Long customerId) {
        Customer customer = findCustomerById(customerId);
        return mapper.toDetailResponse(customer);
    }

    /**
     * 고객사 조회 (코드)
     *
     * @param customerCode 고객사 코드
     * @return 고객사 상세 정보
     */
    public CustomerDto.DetailResponse getCustomerByCode(String customerCode) {
        Customer customer = customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        return mapper.toDetailResponse(customer);
    }

    /**
     * 고객사 목록 조회 (필터링 및 검색)
     *
     * @param isActive 활성화 여부 필터 (true: 활성화만, false: 비활성화만, null: 전체)
     * @param keyword  고객사명 검색 키워드
     * @return 고객사 목록
     */
    public List<CustomerDto.DetailResponse> getCustomers(Boolean isActive, String keyword) {
        List<Customer> customers;

        // 키워드 검색이 있는 경우
        if (keyword != null && !keyword.trim().isEmpty()) {
            customers = customerRepository.findByCustomerNameContaining(keyword.trim());
        }
        // 활성화 여부 필터링
        else if (isActive != null) {
            customers = customerRepository.findAllByIsActive(isActive);
        }
        // 전체 조회
        else {
            customers = customerRepository.findAll();
        }

        return mapper.toDetailResponseList(customers);
    }

    /**
     * 고객사 정보 수정
     *
     * @param customerId    고객사 ID
     * @param request       수정 요청
     * @return 수정된 고객사 상세 정보
     */
    @Transactional
    public CustomerDto.DetailResponse updateCustomer(Long customerId,
            CustomerDto.UpdateRequest request) {
        log.info("Updating customer with customerId: {}", customerId);

        // 엔티티 조회
        Customer customer = findCustomerById(customerId);

        // Setter를 통한 수정 (JPA Dirty Checking)
        if (request.customerName() != null) {
            customer.setCustomerName(request.customerName());
        }
        if (request.description() != null) {
            customer.setDescription(request.description());
        }
        if (request.isActive() != null) {
            customer.setIsActive(request.isActive());
        }

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Customer updated successfully with customerId: {}", customerId);
        return mapper.toDetailResponse(customer);
    }

    /**
     * 고객사 활성화 상태 변경
     *
     * @param customerId 고객사 ID
     * @param isActive   활성화 여부
     */
    @Transactional
    public void updateCustomerStatus(Long customerId, Boolean isActive) {
        log.info("Updating customer status - customerId: {}, isActive: {}", customerId,
                isActive);

        // 엔티티 조회 후 setter를 통한 수정 (JPA Dirty Checking)
        Customer customer = findCustomerById(customerId);
        customer.setIsActive(isActive);

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Customer status updated - customerId: {}, isActive: {}", customerId, isActive);
    }

    /**
     * 고객사 삭제
     *
     * @param customerId 고객사 ID
     */
    @Transactional
    public void deleteCustomer(Long customerId) {
        log.info("Deleting customer with customerId: {}", customerId);

        // 고객사 존재 검증
        Customer customer = findCustomerById(customerId);
        customerRepository.delete(customer);

        log.info("Customer deleted successfully with customerId: {}", customerId);
    }

    // === Private Helper Methods ===

    /**
     * 고객사 조회 (존재하지 않으면 예외 발생)
     */
    private Customer findCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
    }
}
