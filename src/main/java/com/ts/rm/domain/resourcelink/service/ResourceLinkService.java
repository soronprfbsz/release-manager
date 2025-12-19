package com.ts.rm.domain.resourcelink.service;

import com.ts.rm.domain.resourcelink.dto.ResourceLinkDto;
import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import com.ts.rm.domain.resourcelink.repository.ResourceLinkRepository;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ResourceLink 서비스
 *
 * <p>리소스 링크 생성, 조회, 수정, 삭제 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceLinkService {

    private final ResourceLinkRepository resourceLinkRepository;

    /**
     * 리소스 링크 생성
     *
     * @param request 생성 요청 정보
     * @return 생성된 리소스 링크 엔티티
     */
    @Transactional
    public ResourceLink createLink(ResourceLinkDto.CreateRequest request) {
        log.info("리소스 링크 생성 시작 - 링크명: {}, 카테고리: {}, URL: {}",
                request.linkName(), request.linkCategory(), request.linkUrl());

        // 중복 URL 검사
        if (resourceLinkRepository.existsByLinkUrl(request.linkUrl())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "이미 등록된 링크 주소입니다: " + request.linkUrl());
        }

        // sortOrder 자동 채번: 링크 카테고리별로 최대값 + 1
        Integer maxSortOrder = resourceLinkRepository.findMaxSortOrderByLinkCategory(
                request.linkCategory().toUpperCase());
        Integer sortOrder = maxSortOrder + 1;

        // 엔티티 생성 및 저장
        ResourceLink resourceLink = ResourceLink.builder()
                .linkCategory(request.linkCategory().toUpperCase())
                .subCategory(request.subCategory() != null ? request.subCategory().toUpperCase() : null)
                .linkName(request.linkName())
                .linkUrl(request.linkUrl())
                .description(request.description())
                .sortOrder(sortOrder)
                .createdBy(request.createdBy())
                .build();

        ResourceLink saved = resourceLinkRepository.save(resourceLink);
        log.info("리소스 링크 생성 완료 - ID: {}, 링크명: {}", saved.getResourceLinkId(), saved.getLinkName());

        return saved;
    }

    /**
     * 리소스 링크 수정
     *
     * @param id      리소스 링크 ID
     * @param request 수정 요청 정보
     * @return 수정된 리소스 링크 엔티티
     */
    @Transactional
    public ResourceLink updateLink(Long id, ResourceLinkDto.UpdateRequest request) {
        log.info("리소스 링크 수정 시작 - ID: {}, 링크명: {}", id, request.linkName());

        ResourceLink resourceLink = getResourceLink(id);

        // URL이 변경되었고, 변경된 URL이 다른 링크에서 이미 사용 중인지 확인
        if (!resourceLink.getLinkUrl().equals(request.linkUrl()) &&
                resourceLinkRepository.existsByLinkUrl(request.linkUrl())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "이미 등록된 링크 주소입니다: " + request.linkUrl());
        }

        // 엔티티 업데이트
        resourceLink.setLinkCategory(request.linkCategory().toUpperCase());
        resourceLink.setSubCategory(request.subCategory() != null ? request.subCategory().toUpperCase() : null);
        resourceLink.setLinkName(request.linkName());
        resourceLink.setLinkUrl(request.linkUrl());
        resourceLink.setDescription(request.description());

        log.info("리소스 링크 수정 완료 - ID: {}", id);
        return resourceLink;
    }

    /**
     * 리소스 링크 삭제
     *
     * @param id 리소스 링크 ID
     */
    @Transactional
    public void deleteLink(Long id) {
        ResourceLink resourceLink = getResourceLink(id);
        log.info("리소스 링크 삭제 시작 - ID: {}, 링크명: {}", id, resourceLink.getLinkName());

        resourceLinkRepository.delete(resourceLink);
        log.info("리소스 링크 삭제 완료 - ID: {}", id);
    }

    /**
     * 리소스 링크 단건 조회
     */
    public ResourceLink getResourceLink(Long id) {
        return resourceLinkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "리소스 링크를 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 리소스 링크 목록 조회 (sortOrder 오름차순, 생성일시 내림차순)
     */
    public List<ResourceLink> listAllLinks() {
        return resourceLinkRepository.findAllByOrderBySortOrderAscCreatedAtDesc();
    }

    /**
     * 링크 카테고리별 리소스 링크 목록 조회 (sortOrder 오름차순, 생성일시 내림차순)
     *
     * @param linkCategory 링크 카테고리 (DOCUMENT, TOOL, ETC)
     */
    public List<ResourceLink> listLinksByCategory(String linkCategory) {
        return resourceLinkRepository.findByLinkCategoryOrderBySortOrderAscCreatedAtDesc(
                linkCategory.toUpperCase());
    }

    /**
     * 리소스 링크 순서 변경
     *
     * @param request 순서 변경 요청
     */
    @Transactional
    public void reorderResourceLinks(ResourceLinkDto.ReorderResourceLinksRequest request) {
        log.info("리소스 링크 순서 변경 시작 - linkCategory: {}, resourceLinkIds: {}",
                request.linkCategory(), request.resourceLinkIds());

        List<Long> resourceLinkIds = request.resourceLinkIds();
        if (resourceLinkIds == null || resourceLinkIds.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "리소스 링크 ID 목록은 비어있을 수 없습니다");
        }

        // 모든 리소스 링크가 존재하고 동일한 linkCategory인지 확인
        for (Long resourceLinkId : resourceLinkIds) {
            ResourceLink resourceLink = getResourceLink(resourceLinkId);
            if (!request.linkCategory().equalsIgnoreCase(resourceLink.getLinkCategory())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "리소스 링크 " + resourceLinkId + "는 " + request.linkCategory() + " 카테고리에 속하지 않습니다");
            }
        }

        // sortOrder 업데이트 (1부터 시작)
        int sortOrder = 1;
        for (Long resourceLinkId : resourceLinkIds) {
            ResourceLink resourceLink = getResourceLink(resourceLinkId);
            resourceLink.setSortOrder(sortOrder++);
        }

        log.info("리소스 링크 순서 변경 완료 - linkCategory: {}", request.linkCategory());
    }
}
