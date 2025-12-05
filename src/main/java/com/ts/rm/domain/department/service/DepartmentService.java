package com.ts.rm.domain.department.service;

import com.ts.rm.domain.department.dto.DepartmentDto;
import com.ts.rm.domain.department.entity.Department;
import com.ts.rm.domain.department.mapper.DepartmentDtoMapper;
import com.ts.rm.domain.department.repository.DepartmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Department Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentDtoMapper mapper;

    /**
     * 전체 부서 목록 조회
     *
     * @return 부서 목록
     */
    public List<DepartmentDto.Response> getAllDepartments() {
        List<Department> departments = departmentRepository.findAllByOrderByDepartmentNameAsc();
        return mapper.toResponseList(departments);
    }
}
