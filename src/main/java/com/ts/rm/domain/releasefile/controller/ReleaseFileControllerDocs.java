package com.ts.rm.domain.releasefile.controller;

import com.ts.rm.global.response.SwaggerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ReleaseFileController Swagger 문서화 인터페이스
 */
@Tag(name = "릴리즈 파일", description = "릴리즈 파일 관리 API")
@SwaggerResponse
public interface ReleaseFileControllerDocs {

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
}
