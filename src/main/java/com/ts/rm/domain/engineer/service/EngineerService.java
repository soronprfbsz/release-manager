package com.ts.rm.domain.engineer.service;

import com.ts.rm.domain.department.entity.Department;
import com.ts.rm.domain.department.repository.DepartmentRepository;
import com.ts.rm.domain.engineer.dto.EngineerDto;
import com.ts.rm.domain.engineer.entity.Engineer;
import com.ts.rm.domain.engineer.mapper.EngineerDtoMapper;
import com.ts.rm.domain.engineer.repository.EngineerRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import com.ts.rm.global.pagination.PageRowNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Engineer Service
 * - MapStruct를 사용한 Entity ↔ DTO 자동 변환
 * - EngineerDto 단일 클래스에서 Request/Response DTO 관리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EngineerService {

    private final EngineerRepository engineerRepository;
    private final DepartmentRepository departmentRepository;
    private final EngineerDtoMapper mapper;

    /**
     * 엔지니어 생성
     *
     * @param request 생성 요청
     * @param createdBy 생성자 (로그인 사용자 이메일)
     * @return 생성된 엔지니어 정보
     */
    @Transactional
    public EngineerDto.DetailResponse createEngineer(EngineerDto.CreateRequest request, String createdBy) {
        log.info("엔지니어 생성 요청 - email: {}", request.engineerEmail());

        // 이메일 중복 체크
        if (engineerRepository.existsByEngineerEmail(request.engineerEmail())) {
            throw new BusinessException(ErrorCode.ENGINEER_EMAIL_CONFLICT);
        }

        Engineer engineer = mapper.toEntity(request);

        // 부서 설정
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            engineer.setDepartment(department);
        }

        engineer.setCreatedBy(createdBy);
        engineer.setUpdatedBy(createdBy);

        Engineer savedEngineer = engineerRepository.save(engineer);

        log.info("엔지니어 생성 완료 - id: {}", savedEngineer.getEngineerId());
        return mapper.toDetailResponse(savedEngineer);
    }

    /**
     * 엔지니어 조회 (ID)
     *
     * @param engineerId 엔지니어 ID
     * @return 엔지니어 상세 정보
     */
    public EngineerDto.DetailResponse getEngineerById(Long engineerId) {
        Engineer engineer = findEngineerById(engineerId);
        return mapper.toDetailResponse(engineer);
    }

    /**
     * 엔지니어 목록 조회 (페이징)
     * QueryDSL을 사용한 다중 필드 키워드 검색 (이름, 이메일, 직급, 설명)
     *
     * @param departmentId 부서 ID 필터 (null이면 전체)
     * @param keyword 검색 키워드 - 이름, 이메일, 직급, 설명 통합 검색 (null이면 전체)
     * @param pageable 페이징 정보
     * @return 엔지니어 페이지
     */
    public Page<EngineerDto.ListResponse> getEngineers(Long departmentId, String keyword, Pageable pageable) {
        // QueryDSL 기반 다중 필드 검색
        Page<Engineer> engineers = engineerRepository.findAllWithFilters(departmentId, keyword, pageable);

        // rowNumber 계산 (공통 유틸리티 사용)
        return PageRowNumberUtil.mapWithRowNumber(engineers, (engineer, rowNumber) -> {
            EngineerDto.ListResponse response = mapper.toListResponse(engineer);
            return new EngineerDto.ListResponse(
                    rowNumber,
                    response.engineerId(),
                    response.engineerName(),
                    response.engineerEmail(),
                    response.engineerPhone(),
                    response.position(),
                    response.departmentId(),
                    response.departmentName(),
                    response.description(),
                    response.createdAt()
            );
        });
    }

    /**
     * 엔지니어 정보 수정
     *
     * @param engineerId 엔지니어 ID
     * @param request 수정 요청
     * @param updatedBy 수정자 (로그인 사용자 이메일)
     * @return 수정된 엔지니어 정보
     */
    @Transactional
    public EngineerDto.DetailResponse updateEngineer(Long engineerId, EngineerDto.UpdateRequest request, String updatedBy) {
        log.info("엔지니어 수정 요청 - id: {}", engineerId);

        Engineer engineer = findEngineerById(engineerId);

        // 이메일 변경 시 중복 체크
        if (request.engineerEmail() != null && !request.engineerEmail().equals(engineer.getEngineerEmail())) {
            if (engineerRepository.existsByEngineerEmail(request.engineerEmail())) {
                throw new BusinessException(ErrorCode.ENGINEER_EMAIL_CONFLICT);
            }
            engineer.setEngineerEmail(request.engineerEmail());
        }

        // 나머지 필드 업데이트 (null이 아닌 경우에만)
        if (request.engineerName() != null) {
            engineer.setEngineerName(request.engineerName());
        }
        if (request.engineerPhone() != null) {
            engineer.setEngineerPhone(request.engineerPhone());
        }
        if (request.position() != null) {
            engineer.setPosition(request.position());
        }
        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            engineer.setDepartment(department);
        }
        if (request.description() != null) {
            engineer.setDescription(request.description());
        }

        engineer.setUpdatedBy(updatedBy);

        log.info("엔지니어 수정 완료 - id: {}", engineerId);
        return mapper.toDetailResponse(engineer);
    }

    /**
     * 엔지니어 삭제
     *
     * @param engineerId 엔지니어 ID
     */
    @Transactional
    public void deleteEngineer(Long engineerId) {
        log.info("엔지니어 삭제 요청 - id: {}", engineerId);

        if (!engineerRepository.existsById(engineerId)) {
            throw new BusinessException(ErrorCode.ENGINEER_NOT_FOUND);
        }

        engineerRepository.deleteById(engineerId);

        log.info("엔지니어 삭제 완료 - id: {}", engineerId);
    }

    /**
     * 엔지니어 조회 (내부용)
     */
    private Engineer findEngineerById(Long engineerId) {
        return engineerRepository.findById(engineerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENGINEER_NOT_FOUND));
    }
}
