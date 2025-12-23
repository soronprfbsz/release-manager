package com.ts.rm.domain.resourcelink.controller;

import com.ts.rm.domain.resourcelink.dto.ResourceLinkDto;
import com.ts.rm.domain.resourcelink.entity.ResourceLink;
import com.ts.rm.domain.resourcelink.mapper.ResourceLinkDtoMapper;
import com.ts.rm.domain.resourcelink.service.ResourceLinkService;
import com.ts.rm.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 리소스 링크 API Controller
 *
 * <p>구글 시트, 노션 등 외부 리소스 링크 관리 API
 */
@Slf4j
@RestController
@RequestMapping("/api/resources/links")
@RequiredArgsConstructor
public class ResourceLinkController implements ResourceLinkControllerDocs {

    private final ResourceLinkService resourceLinkService;
    private final ResourceLinkDtoMapper resourceLinkDtoMapper;

    /**
     * 리소스 링크 생성
     */
    @Override
    @PostMapping
    public ApiResponse<ResourceLinkDto.DetailResponse> createLink(
            @RequestBody @Valid ResourceLinkDto.CreateRequest request) {

        log.info("리소스 링크 생성 요청 - 링크명: {}, 카테고리: {}, URL: {}",
                request.linkName(), request.linkCategory(), request.linkUrl());

        ResourceLink resourceLink = resourceLinkService.createLink(request);
        ResourceLinkDto.DetailResponse response = resourceLinkDtoMapper.toDetailResponse(resourceLink);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 링크 수정
     */
    @Override
    @PutMapping("/{id}")
    public ApiResponse<ResourceLinkDto.DetailResponse> updateLink(
            @PathVariable Long id,
            @RequestBody @Valid ResourceLinkDto.UpdateRequest request) {

        log.info("리소스 링크 수정 요청 - ID: {}, 링크명: {}", id, request.linkName());

        ResourceLink resourceLink = resourceLinkService.updateLink(id, request);
        ResourceLinkDto.DetailResponse response = resourceLinkDtoMapper.toDetailResponse(resourceLink);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 링크 상세 조회
     */
    @Override
    @GetMapping("/{id}")
    public ApiResponse<ResourceLinkDto.DetailResponse> getResourceLink(@PathVariable Long id) {

        log.info("리소스 링크 조회 요청 - ID: {}", id);

        ResourceLink resourceLink = resourceLinkService.getResourceLink(id);
        ResourceLinkDto.DetailResponse response = resourceLinkDtoMapper.toDetailResponse(resourceLink);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 링크 목록 조회
     */
    @Override
    @GetMapping
    public ApiResponse<List<ResourceLinkDto.SimpleResponse>> listResourceLinks(
            @RequestParam(required = false) String linkCategory,
            @RequestParam(required = false) String keyword) {

        log.info("리소스 링크 목록 조회 요청 - 링크카테고리: {}, 키워드: {}", linkCategory, keyword);

        List<ResourceLink> resourceLinks = resourceLinkService.listLinksWithFilters(linkCategory, keyword);
        List<ResourceLinkDto.SimpleResponse> response = resourceLinkDtoMapper.toSimpleResponseList(resourceLinks);

        return ApiResponse.success(response);
    }

    /**
     * 리소스 링크 삭제
     */
    @Override
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteResourceLink(@PathVariable Long id) {

        log.info("리소스 링크 삭제 요청 - ID: {}", id);

        resourceLinkService.deleteLink(id);

        return ApiResponse.success(null);
    }

    /**
     * 리소스 링크 순서 변경
     */
    @Override
    @PatchMapping("/order")
    public ApiResponse<Void> reorderResourceLinks(
            @RequestBody @Valid ResourceLinkDto.ReorderResourceLinksRequest request) {

        log.info("리소스 링크 순서 변경 요청 - IDs: {}", request.resourceLinkIds());

        resourceLinkService.reorderResourceLinks(request);

        return ApiResponse.success(null);
    }
}
