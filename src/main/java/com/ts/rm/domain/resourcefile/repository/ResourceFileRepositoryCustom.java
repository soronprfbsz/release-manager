package com.ts.rm.domain.resourcefile.repository;

import com.ts.rm.domain.resourcefile.entity.ResourceFile;
import java.util.List;

/**
 * ResourceFile Repository Custom Interface
 *
 * <p>QueryDSL을 사용한 복잡한 리소스 파일 쿼리 인터페이스
 */
public interface ResourceFileRepositoryCustom {

    /**
     * 리소스 파일 목록 조회 (카테고리 필터링 + 키워드 검색)
     *
     * @param fileCategory 파일 카테고리 (null이면 전체)
     * @param keyword 검색 키워드 (리소스파일명, 파일명, 설명)
     * @return 리소스 파일 목록
     */
    List<ResourceFile> findAllWithFilters(String fileCategory, String keyword);
}
