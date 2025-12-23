package com.ts.rm.domain.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

/**
 * Service DTO 통합 클래스
 */
public final class ServiceDto {

    private ServiceDto() {
    }

    // ========================================
    // Request DTOs
    // ========================================

    /**
     * 서비스 생성 요청
     */
    @Builder
    @Schema(description = "서비스 생성 요청")
    public record CreateRequest(
            @Schema(description = "서비스명", example = "Infraeye 1 운영 환경")
            @NotBlank(message = "서비스명은 필수입니다")
            @Size(max = 255, message = "서비스명은 255자 이하여야 합니다")
            String serviceName,

            @Schema(description = "서비스 분류 (SERVICE_TYPE 코드)", example = "infraeye1", allowableValues = {"infraeye1", "infraeye2", "infra", "etc"})
            @NotBlank(message = "서비스 분류는 필수입니다")
            @Size(max = 50, message = "서비스 분류는 50자 이하여야 합니다")
            String serviceType,

            @Schema(description = "설명", example = "Infraeye 1 운영 환경 접속 정보")
            String description,

            @Schema(description = "컴포넌트 목록")
            List<ComponentRequest> components
    ) {
    }

    /**
     * 서비스 수정 요청
     */
    @Builder
    @Schema(description = "서비스 수정 요청")
    public record UpdateRequest(
            @Schema(description = "서비스명", example = "Infraeye 1 운영 환경")
            @Size(max = 255, message = "서비스명은 255자 이하여야 합니다")
            String serviceName,

            @Schema(description = "서비스 분류 (SERVICE_TYPE 코드)", example = "infraeye1", allowableValues = {"infraeye1", "infraeye2", "infra", "etc"})
            @Size(max = 50, message = "서비스 분류는 50자 이하여야 합니다")
            String serviceType,

            @Schema(description = "설명", example = "Infraeye 1 운영 환경 접속 정보")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive
    ) {
    }

    /**
     * 컴포넌트 (접속 정보) 요청
     */
    @Builder
    @Schema(description = "컴포넌트 (접속 정보) 요청")
    public record ComponentRequest(
            @Schema(description = "컴포넌트 타입 (COMPONENT_TYPE 코드)", example = "DATABASE", allowableValues = {"WEB", "DATABASE", "ENGINE", "ETC"})
            @NotBlank(message = "컴포넌트 타입은 필수입니다")
            String componentType,

            @Schema(description = "컴포넌트명", example = "운영 DB")
            @NotBlank(message = "컴포넌트명은 필수입니다")
            @Size(max = 255, message = "컴포넌트명은 255자 이하여야 합니다")
            String componentName,

            @Schema(description = "호스트 주소", example = "192.168.1.100")
            @NotBlank(message = "호스트 주소는 필수입니다")
            @Size(max = 255, message = "호스트 주소는 255자 이하여야 합니다")
            String host,

            @Schema(description = "포트 번호", example = "3306")
            @NotNull(message = "포트 번호는 필수입니다")
            Integer port,

            @Schema(description = "URL (WEB 타입용)", example = "https://example.com")
            @Size(max = 500, message = "URL은 500자 이하여야 합니다")
            String url,

            @Schema(description = "SSH 포트번호", example = "22")
            Integer sshPort,

            @Schema(description = "설명", example = "운영 데이터베이스 접속 정보")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive
    ) {
    }

    /**
     * 서비스 순서 변경 요청
     */
    @Builder
    @Schema(description = "서비스 순서 변경 요청")
    public record ReorderServicesRequest(
            @Schema(description = "서비스 분류 (SERVICE_TYPE 코드)", example = "infraeye1", allowableValues = {"infraeye1", "infraeye2", "infra", "etc"})
            @NotBlank(message = "서비스 분류는 필수입니다")
            String serviceType,

            @Schema(description = "정렬할 서비스 ID 목록 (순서대로)", example = "[1, 3, 2, 5, 4]")
            @NotNull(message = "서비스 ID 목록은 필수입니다")
            List<Long> serviceIds
    ) {
    }

    /**
     * 컴포넌트 순서 변경 요청
     */
    @Builder
    @Schema(description = "컴포넌트 순서 변경 요청")
    public record ReorderComponentsRequest(
            @Schema(description = "정렬할 컴포넌트 ID 목록 (순서대로)", example = "[3, 1, 2, 5, 4]")
            @NotNull(message = "컴포넌트 ID 목록은 필수입니다")
            List<Long> componentIds
    ) {
    }

    // ========================================
    // Response DTOs
    // ========================================

    /**
     * 서비스 상세 응답
     */
    @Schema(description = "서비스 상세 응답")
    public record DetailResponse(
            @Schema(description = "서비스 ID", example = "1")
            Long serviceId,

            @Schema(description = "서비스명", example = "Infraeye 1 운영 환경")
            String serviceName,

            @Schema(description = "서비스 분류 코드", example = "infraeye1")
            String serviceType,

            @Schema(description = "서비스 분류명", example = "Infraeye 1")
            String serviceTypeName,

            @Schema(description = "설명", example = "Infraeye 1 운영 환경 접속 정보")
            String description,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive,

            @Schema(description = "컴포넌트 목록")
            List<ComponentResponse> components,

            @Schema(description = "생성일시")
            LocalDateTime createdAt,

            @Schema(description = "생성자", example = "admin@example.com")
            String createdBy,

            @Schema(description = "수정일시")
            LocalDateTime updatedAt,

            @Schema(description = "수정자", example = "user@example.com")
            String updatedBy
    ) {
    }

    /**
     * 서비스 목록 응답 (간단)
     */
    @Schema(description = "서비스 목록 응답")
    public record SimpleResponse(
            @Schema(description = "서비스 ID", example = "1")
            Long serviceId,

            @Schema(description = "서비스명", example = "Infraeye 1 운영 환경")
            String serviceName,

            @Schema(description = "서비스 분류 코드", example = "infraeye1")
            String serviceType,

            @Schema(description = "서비스 분류명", example = "Infraeye 1")
            String serviceTypeName,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive,

            @Schema(description = "컴포넌트 개수", example = "3")
            Integer componentCount,

            @Schema(description = "생성일시")
            LocalDateTime createdAt
    ) {
    }

    /**
     * 컴포넌트 (접속 정보) 응답
     */
    @Schema(description = "컴포넌트 (접속 정보) 응답")
    public record ComponentResponse(
            @Schema(description = "컴포넌트 ID", example = "1")
            Long componentId,

            @Schema(description = "컴포넌트 타입 코드", example = "DATABASE")
            String componentType,

            @Schema(description = "컴포넌트 타입명", example = "데이터베이스")
            String componentTypeName,

            @Schema(description = "컴포넌트명", example = "운영 DB")
            String componentName,

            @Schema(description = "호스트 주소", example = "192.168.1.100")
            String host,

            @Schema(description = "포트 번호", example = "3306")
            Integer port,

            @Schema(description = "URL", example = "https://example.com")
            String url,

            @Schema(description = "SSH 포트번호", example = "22")
            Integer sshPort,

            @Schema(description = "설명", example = "운영 데이터베이스 접속 정보")
            String description,

            @Schema(description = "정렬 순서", example = "1")
            Integer sortOrder,

            @Schema(description = "활성 여부", example = "true")
            Boolean isActive
    ) {
    }
}
