package com.ts.rm.domain.common.repository;

import com.ts.rm.domain.common.entity.Code;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepository extends JpaRepository<Code, String> {
}
