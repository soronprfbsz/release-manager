package com.ts.rm.domain.service.controller;

import com.ts.rm.domain.service.dto.ServiceDto;
import com.ts.rm.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Service Controller API 문서
 */
@Tag(name = "서비스 관리", description = "서비스 관리 API")
public interface ServiceControllerDocs {

    @Operation(summary = "서비스 생성", description = "새로운 서비스를 생성합니다 (컴포넌트 포함)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "서비스 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "서비스 생성 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "serviceId": 1,
                                                "serviceName": "Infraeye 1 운영 환경",
                                                "serviceType": "infraeye1",
                                                "serviceTypeName": "Infraeye 1",
                                                "description": "Infraeye 1 운영 환경 접속 정보",
                                                "isActive": true,
                                                "components": [
                                                  {
                                                    "componentId": 1,
                                                    "componentType": "DATABASE",
                                                    "componentTypeName": "데이터베이스",
                                                    "componentName": "운영 DB",
                                                    "host": "192.168.1.100",
                                                    "port": 3306,
                                                    "url": null,
                                                    "sshPort": 22,
                                                    "description": "운영 데이터베이스",
                                                    "sortOrder": 1,
                                                    "isActive": true
                                                  },
                                                  {
                                                    "componentId": 2,
                                                    "componentType": "WEB",
                                                    "componentTypeName": "웹",
                                                    "componentName": "운영 웹서버",
                                                    "host": "192.168.1.101",
                                                    "port": 8080,
                                                    "url": "https://infraeye1.example.com",
                                                    "sshPort": null,
                                                    "description": "운영 웹 애플리케이션",
                                                    "sortOrder": 2,
                                                    "isActive": true
                                                  }
                                                ],
                                                "createdAt": "2025-12-12T10:30:00",
                                                "createdBy": "admin@example.com",
                                                "updatedAt": "2025-12-12T10:30:00",
                                                "updatedBy": null
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    ResponseEntity<ApiResponse<ServiceDto.DetailResponse>> createService(
            @Valid @RequestBody ServiceDto.CreateRequest request);

    @Operation(summary = "서비스 상세 조회", description = "서비스 ID로 서비스 정보를 조회합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "서비스 상세 조회 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "serviceId": 1,
                                                "serviceName": "Infraeye 1 운영 환경",
                                                "serviceType": "infraeye1",
                                                "serviceTypeName": "Infraeye 1",
                                                "description": "Infraeye 1 운영 환경 접속 정보",
                                                "isActive": true,
                                                "components": [
                                                  {
                                                    "componentId": 1,
                                                    "componentType": "DATABASE",
                                                    "componentTypeName": "데이터베이스",
                                                    "componentName": "운영 DB",
                                                    "host": "192.168.1.100",
                                                    "port": 3306,
                                                    "url": null,
                                                    "sshPort": 22,
                                                    "description": "운영 데이터베이스",
                                                    "sortOrder": 1,
                                                    "isActive": true
                                                  }
                                                ],
                                                "createdAt": "2025-12-12T10:30:00",
                                                "createdBy": "admin@example.com",
                                                "updatedAt": "2025-12-12T10:30:00",
                                                "updatedBy": null
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ServiceDto.DetailResponse>> getServiceById(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id);

    @Operation(summary = "서비스 목록 조회", description = "서비스 목록을 조회합니다 (컴포넌트 정보 포함)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "서비스 목록 조회 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": [
                                                {
                                                  "serviceId": 1,
                                                  "serviceName": "Infraeye 1 운영 환경",
                                                  "serviceType": "infraeye1",
                                                  "serviceTypeName": "Infraeye 1",
                                                  "description": "Infraeye 1 운영 환경 접속 정보",
                                                  "isActive": true,
                                                  "components": [
                                                    {
                                                      "componentId": 1,
                                                      "componentType": "DATABASE",
                                                      "componentTypeName": "데이터베이스",
                                                      "componentName": "운영 DB",
                                                      "host": "192.168.1.100",
                                                      "port": 3306,
                                                      "url": null,
                                                      "sshPort": 22,
                                                      "description": "운영 데이터베이스",
                                                      "sortOrder": 1,
                                                      "isActive": true
                                                    }
                                                  ],
                                                  "createdAt": "2025-12-12T10:30:00",
                                                  "createdBy": "admin@example.com",
                                                  "updatedAt": "2025-12-12T10:30:00",
                                                  "updatedBy": null
                                                },
                                                {
                                                  "serviceId": 2,
                                                  "serviceName": "Infraeye 2 운영 환경",
                                                  "serviceType": "infraeye2",
                                                  "serviceTypeName": "Infraeye 2",
                                                  "description": "Infraeye 2 운영 환경 접속 정보",
                                                  "isActive": true,
                                                  "components": [
                                                    {
                                                      "componentId": 3,
                                                      "componentType": "WEB",
                                                      "componentTypeName": "웹",
                                                      "componentName": "운영 웹서버",
                                                      "host": "192.168.2.100",
                                                      "port": 8080,
                                                      "url": "https://infraeye2.example.com",
                                                      "sshPort": null,
                                                      "description": "운영 웹 애플리케이션",
                                                      "sortOrder": 1,
                                                      "isActive": true
                                                    }
                                                  ],
                                                  "createdAt": "2025-12-12T11:00:00",
                                                  "createdBy": "admin@example.com",
                                                  "updatedAt": "2025-12-12T11:00:00",
                                                  "updatedBy": null
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<ApiResponse<List<ServiceDto.DetailResponse>>> getServices(
            @Parameter(description = "서비스 분류 필터", example = "infraeye1")
            @RequestParam(required = false) String serviceType,

            @Parameter(description = "검색 키워드 (서비스명, 서비스타입, 설명 통합 검색)", example = "운영")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "활성 상태 필터", example = "true")
            @RequestParam(required = false) Boolean isActive);

    @Operation(summary = "서비스 수정", description = "서비스 정보를 수정합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "서비스 수정 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "serviceId": 1,
                                                "serviceName": "Infraeye 1 운영 환경 (수정됨)",
                                                "serviceType": "infraeye1",
                                                "serviceTypeName": "Infraeye 1",
                                                "description": "수정된 설명",
                                                "isActive": true,
                                                "components": [
                                                  {
                                                    "componentId": 1,
                                                    "componentType": "DATABASE",
                                                    "componentTypeName": "데이터베이스",
                                                    "componentName": "운영 DB",
                                                    "host": "192.168.1.100",
                                                    "port": 3306,
                                                    "url": null,
                                                    "sshPort": 22,
                                                    "description": "운영 데이터베이스",
                                                    "sortOrder": 1,
                                                    "isActive": true
                                                  }
                                                ],
                                                "createdAt": "2025-12-12T10:30:00",
                                                "createdBy": "admin@example.com",
                                                "updatedAt": "2025-12-12T14:00:00",
                                                "updatedBy": "user@example.com"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ServiceDto.DetailResponse>> updateService(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ServiceDto.UpdateRequest request);

    @Operation(summary = "서비스 삭제", description = "서비스를 삭제합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
    })
    ResponseEntity<Void> deleteService(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id);

    @Operation(summary = "컴포넌트 추가", description = "서비스에 새로운 컴포넌트(접속 정보)를 추가합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "컴포넌트 추가 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "컴포넌트 추가 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "componentId": 5,
                                                "componentType": "ENGINE",
                                                "componentTypeName": "엔진",
                                                "componentName": "분석 엔진",
                                                "host": "192.168.1.105",
                                                "port": 9000,
                                                "url": null,
                                                "sshPort": 22,
                                                "description": "데이터 분석 엔진",
                                                "sortOrder": 3,
                                                "isActive": true
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ServiceDto.ComponentResponse>> addComponent(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ServiceDto.ComponentRequest request);

    @Operation(summary = "컴포넌트 수정", description = "컴포넌트(접속 정보)를 수정합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    name = "컴포넌트 수정 성공 예시",
                                    value = """
                                            {
                                              "status": "success",
                                              "data": {
                                                "componentId": 1,
                                                "componentType": "DATABASE",
                                                "componentTypeName": "데이터베이스",
                                                "componentName": "운영 DB (수정됨)",
                                                "host": "192.168.1.100",
                                                "port": 3307,
                                                "url": null,
                                                "sshPort": 22,
                                                "description": "수정된 데이터베이스 설명",
                                                "sortOrder": 1,
                                                "isActive": true
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "컴포넌트를 찾을 수 없음")
    })
    ResponseEntity<ApiResponse<ServiceDto.ComponentResponse>> updateComponent(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "컴포넌트 ID", example = "1") @PathVariable Long componentId,
            @Valid @RequestBody ServiceDto.ComponentRequest request);

    @Operation(summary = "컴포넌트 삭제", description = "컴포넌트(접속 정보)를 삭제합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "컴포넌트를 찾을 수 없음")
    })
    ResponseEntity<Void> deleteComponent(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "컴포넌트 ID", example = "1") @PathVariable Long componentId);

    @Operation(
            summary = "서비스 순서 변경",
            description = """
                    특정 서비스 분류(serviceType) 내에서 서비스 목록의 표시 순서를 변경합니다.

                    **동작 방식**:
                    - 동일한 serviceType에 속한 서비스들의 순서만 변경 가능
                    - 요청받은 서비스 ID 순서대로 sortOrder를 1부터 재부여

                    **요청 예시**:
                    ```json
                    {
                      "serviceType": "infraeye1",
                      "serviceIds": [3, 1, 2, 5, 4]
                    }
                    ```

                    **주의사항**:
                    - 모든 서비스 ID가 존재해야 함
                    - 모든 서비스가 지정한 serviceType에 속해야 함
                    - 빈 목록은 허용되지 않음
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "순서 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서비스를 찾을 수 없음")
    })
    ResponseEntity<Void> reorderServices(
            @Valid @RequestBody ServiceDto.ReorderServicesRequest request);

    @Operation(summary = "컴포넌트 순서 변경", description = "서비스 내 컴포넌트의 표시 순서를 변경합니다")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "순서 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "서비스 또는 컴포넌트를 찾을 수 없음")
    })
    ResponseEntity<Void> reorderComponents(
            @Parameter(description = "서비스 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody ServiceDto.ReorderComponentsRequest request);
}
