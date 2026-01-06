package com.ts.rm.domain.service.controller;

import com.ts.rm.domain.service.dto.ServiceDto;
import com.ts.rm.domain.service.service.ServiceService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Service Controller
 *
 * <p>서비스 관리 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController implements ServiceControllerDocs {

    private final ServiceService serviceService;

    /**
     * 서비스 생성
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<ServiceDto.DetailResponse>> createService(
            @Valid @RequestBody ServiceDto.CreateRequest request) {

        log.info("서비스 생성 요청 - serviceName: {}, serviceType: {}",
                request.serviceName(), request.serviceType());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        log.info("서비스 생성자 정보: email={}, role={}", tokenInfo.email(), tokenInfo.role());

        ServiceDto.DetailResponse response = serviceService.createService(request, tokenInfo.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 서비스 상세 조회
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDto.DetailResponse>> getServiceById(@PathVariable Long id) {
        log.info("서비스 조회 - id: {}", id);
        ServiceDto.DetailResponse response = serviceService.getServiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 서비스 목록 조회 (컴포넌트 포함)
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<ServiceDto.DetailResponse>>> getServices(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String keyword) {

        log.info("서비스 목록 조회 - serviceType: {}, keyword: {}",
                serviceType, keyword);

        List<ServiceDto.DetailResponse> response = serviceService.getServices(
                serviceType, keyword);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 서비스 수정
     */
    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<ServiceDto.DetailResponse>> updateService(
            @PathVariable Long id,
            @Valid @RequestBody ServiceDto.UpdateRequest request) {

        log.info("서비스 수정 요청 - id: {}", id);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        ServiceDto.DetailResponse response = serviceService.updateService(id, request, tokenInfo.email());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 서비스 삭제
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        log.info("서비스 삭제 요청 - id: {}", id);
        serviceService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 컴포넌트 추가
     */
    @Override
    @PostMapping("/{id}/components")
    public ResponseEntity<ApiResponse<ServiceDto.ComponentResponse>> addComponent(
            @PathVariable Long id,
            @Valid @RequestBody ServiceDto.ComponentRequest request) {

        log.info("컴포넌트 추가 요청 - serviceId: {}, componentName: {}", id, request.componentName());

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        ServiceDto.ComponentResponse response = serviceService.addComponent(id, request, tokenInfo.email());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    /**
     * 컴포넌트 수정
     */
    @Override
    @PatchMapping("/{id}/components/{componentId}")
    public ResponseEntity<ApiResponse<ServiceDto.ComponentResponse>> updateComponent(
            @PathVariable Long id,
            @PathVariable Long componentId,
            @Valid @RequestBody ServiceDto.ComponentRequest request) {

        log.info("컴포넌트 수정 요청 - serviceId: {}, componentId: {}", id, componentId);

        TokenInfo tokenInfo = SecurityUtil.getTokenInfo();
        ServiceDto.ComponentResponse response = serviceService.updateComponent(id, componentId, request, tokenInfo.email());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 컴포넌트 삭제
     */
    @Override
    @DeleteMapping("/{id}/components/{componentId}")
    public ResponseEntity<Void> deleteComponent(
            @PathVariable Long id,
            @PathVariable Long componentId) {

        log.info("컴포넌트 삭제 요청 - serviceId: {}, componentId: {}", id, componentId);
        serviceService.deleteComponent(id, componentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 서비스 순서 변경
     */
    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorderServices(
            @Valid @RequestBody ServiceDto.ReorderServicesRequest request) {

        log.info("서비스 순서 변경 요청 - serviceIds: {}", request.serviceIds());
        serviceService.reorderServices(request);
        return ResponseEntity.ok().build();
    }

    /**
     * 컴포넌트 순서 변경
     */
    @Override
    @PatchMapping("/{id}/components/order")
    public ResponseEntity<Void> reorderComponents(
            @PathVariable Long id,
            @Valid @RequestBody ServiceDto.ReorderComponentsRequest request) {

        log.info("컴포넌트 순서 변경 요청 - serviceId: {}, componentIds: {}", id, request.componentIds());
        serviceService.reorderComponents(id, request);
        return ResponseEntity.ok().build();
    }
}
