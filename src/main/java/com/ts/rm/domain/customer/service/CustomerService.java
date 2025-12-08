package com.ts.rm.domain.customer.service;

import com.ts.rm.domain.customer.dto.CustomerDto;
import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.entity.CustomerProject;
import com.ts.rm.domain.customer.mapper.CustomerDtoMapper;
import com.ts.rm.domain.customer.repository.CustomerProjectRepository;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.project.entity.Project;
import com.ts.rm.domain.project.repository.ProjectRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.pagination.PageRowNumberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final CustomerProjectRepository customerProjectRepository;
    private final ProjectRepository projectRepository;
    private final CustomerDtoMapper mapper;

    /**
     * 고객사 생성
     *
     * @param request   고객사 생성 요청
     * @param createdBy 생성자 (JWT에서 추출)
     * @return 생성된 고객사 상세 정보
     */
    @Transactional
    public CustomerDto.DetailResponse createCustomer(CustomerDto.CreateRequest request, String createdBy) {
        log.info("Creating customer with code: {}", request.customerCode());

        // 중복 검증
        if (customerRepository.existsByCustomerCode(request.customerCode())) {
            throw new BusinessException(ErrorCode.CUSTOMER_CODE_CONFLICT);
        }

        Customer customer = mapper.toEntity(request);
        customer.setCreatedBy(createdBy);
        customer.setUpdatedBy(createdBy);

        Customer savedCustomer = customerRepository.save(customer);

        // 프로젝트 연결 처리
        CustomerDto.ProjectInfo projectInfo = null;
        if (request.projectId() != null && !request.projectId().isBlank()) {
            projectInfo = saveCustomerProject(savedCustomer, request.projectId());
        }

        log.info("Customer created successfully with id: {}", savedCustomer.getCustomerId());
        return toDetailResponseWithProject(savedCustomer, projectInfo);
    }

    /**
     * 고객사 조회 (ID)
     *
     * @param customerId 고객사 ID
     * @return 고객사 상세 정보
     */
    public CustomerDto.DetailResponse getCustomerById(Long customerId) {
        Customer customer = findCustomerById(customerId);
        CustomerDto.ProjectInfo projectInfo = getProjectInfoByCustomerId(customerId);
        return toDetailResponseWithProject(customer, projectInfo);
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
        CustomerDto.ProjectInfo projectInfo = getProjectInfoByCustomerId(customer.getCustomerId());
        return toDetailResponseWithProject(customer, projectInfo);
    }

    /**
     * 고객사 목록 조회 (필터링 및 검색) - 비페이징
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

        return customers.stream()
                .map(customer -> {
                    CustomerDto.ProjectInfo projectInfo = getProjectInfoByCustomerId(customer.getCustomerId());
                    return toDetailResponseWithProject(customer, projectInfo);
                })
                .toList();
    }

    /**
     * 고객사 목록 페이징 조회 (필터링 및 검색)
     *
     * @param isActive 활성화 여부 필터 (true: 활성화만, false: 비활성화만, null: 전체)
     * @param keyword  고객사명 검색 키워드
     * @param pageable 페이징 정보 (sort에 "project.projectName", "lastPatchedVersion", "lastPatchedAt" 사용 가능)
     * @return 고객사 페이지
     */
    public Page<CustomerDto.ListResponse> getCustomersWithPaging(Boolean isActive, String keyword, Pageable pageable) {
        // QueryDSL Custom 메서드 사용 (프로젝트 정보 JOIN 정렬 지원)
        Page<Customer> customers = customerRepository.findAllWithProjectInfo(isActive, keyword, pageable);

        // rowNumber 계산 (공통 유틸리티 사용)
        return PageRowNumberUtil.mapWithRowNumber(customers, (customer, rowNumber) -> {
            CustomerDto.ProjectInfo projectInfo = getProjectInfoByCustomerId(customer.getCustomerId());
            return new CustomerDto.ListResponse(
                    rowNumber,
                    customer.getCustomerId(),
                    customer.getCustomerCode(),
                    customer.getCustomerName(),
                    customer.getDescription(),
                    customer.getIsActive(),
                    projectInfo,
                    customer.getCreatedAt()
            );
        });
    }

    /**
     * 고객사 정보 수정
     *
     * <p>프로젝트 정보는 수정 불가 (기존 프로젝트 정보 유지)
     *
     * @param customerId    고객사 ID
     * @param request       수정 요청
     * @param updatedBy     수정자 (JWT에서 추출)
     * @return 수정된 고객사 상세 정보
     */
    @Transactional
    public CustomerDto.DetailResponse updateCustomer(Long customerId,
            CustomerDto.UpdateRequest request, String updatedBy) {
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
        // updatedBy는 항상 설정 (JWT에서 추출)
        customer.setUpdatedBy(updatedBy);

        // 기존 프로젝트 정보 조회 (프로젝트는 수정 불가)
        CustomerDto.ProjectInfo projectInfo = getProjectInfoByCustomerId(customerId);

        // 트랜잭션 커밋 시 자동으로 UPDATE 쿼리 실행 (Dirty Checking)
        log.info("Customer updated successfully with customerId: {}", customerId);
        return toDetailResponseWithProject(customer, projectInfo);
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

    /**
     * 고객사 ID로 프로젝트 정보 조회 (단일 프로젝트)
     */
    private CustomerDto.ProjectInfo getProjectInfoByCustomerId(Long customerId) {
        List<CustomerProject> customerProjects = customerProjectRepository.findAllByCustomerIdWithProject(customerId);
        if (customerProjects.isEmpty()) {
            return null;
        }
        // 첫 번째 프로젝트만 반환 (현재 고객사당 하나의 프로젝트만 사용)
        CustomerProject cp = customerProjects.get(0);
        return new CustomerDto.ProjectInfo(
                cp.getProject().getProjectId(),
                cp.getProject().getProjectName(),
                cp.getLastPatchedVersion(),
                cp.getLastPatchedAt()
        );
    }

    /**
     * 고객사 생성 시 프로젝트 연결 저장
     */
    private CustomerDto.ProjectInfo saveCustomerProject(Customer customer, String projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND,
                        "프로젝트를 찾을 수 없습니다: " + projectId));

        CustomerProject customerProject = CustomerProject.create(customer, project);
        customerProjectRepository.save(customerProject);

        return new CustomerDto.ProjectInfo(
                project.getProjectId(),
                project.getProjectName(),
                null,
                null
        );
    }

    /**
     * Customer 엔티티와 프로젝트 정보로 DetailResponse 생성
     */
    private CustomerDto.DetailResponse toDetailResponseWithProject(Customer customer,
            CustomerDto.ProjectInfo projectInfo) {
        return new CustomerDto.DetailResponse(
                customer.getCustomerId(),
                customer.getCustomerCode(),
                customer.getCustomerName(),
                customer.getDescription(),
                customer.getIsActive(),
                projectInfo,
                customer.getCreatedAt(),
                customer.getCreatedBy(),
                customer.getUpdatedAt(),
                customer.getUpdatedBy()
        );
    }
}
