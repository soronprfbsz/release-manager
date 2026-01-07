package com.ts.rm.domain.releasefile.controller;

import com.ts.rm.domain.releasefile.dto.ReleaseFileDto;
import com.ts.rm.global.response.ApiResponse;
import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ReleaseFileController Swagger 문서화 인터페이스
 */
@Tag(name = "릴리즈 파일", description = "릴리즈 파일 관리 API")
@SwaggerResponse
public interface ReleaseFileControllerDocs {

    @Operation(
            summary = "릴리즈 파일 다운로드",
            description = "릴리즈 파일을 다운로드합니다"
    )
    ResponseEntity<Resource> downloadReleaseFile(
            @Parameter(description = "릴리즈 파일 ID", required = true)
            @PathVariable("id") Long fileId
    );

    @Operation(
            summary = "버전별 파일 일괄 다운로드 (스트리밍)",
            description = "특정 버전의 모든 파일을 ZIP 형식으로 스트리밍 다운로드합니다.\n\n"
                    + "각 폴더 내에는 실행 순서대로 정렬된 파일들이 포함됩니다.\n\n"
                    + "**응답 헤더**:\n"
                    + "- `X-Uncompressed-Size`: 압축 전 총 파일 크기 (바이트) - 진행률 표시용"
    )
    void downloadVersionFiles(
            @Parameter(description = "릴리즈 버전 ID", required = true)
            @PathVariable("versionId") Long versionId,
            HttpServletResponse response
    ) throws IOException;

    @Operation(
            summary = "릴리즈 파일 내용 조회",
            description = "릴리즈 파일의 내용을 조회합니다.\n\n"
                    + "**파일 타입별 응답**:\n"
                    + "- 텍스트 파일 (SQL, TXT 등): `isBinary=false`, `content`는 UTF-8 문자열\n"
                    + "- 바이너리 파일 (이미지, 폰트 등): `isBinary=true`, `content`는 Base64 인코딩\n\n"
                    + "**최대 파일 크기**: 10MB"
    )
    ResponseEntity<ApiResponse<ReleaseFileDto.FileContentResponse>> getFileContent(
            @Parameter(description = "릴리즈 파일 ID", required = true)
            @PathVariable("id") Long fileId
    );
}
