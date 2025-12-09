package com.ts.rm.global.response;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Swagger 응답 예시 문서화
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(
        value = {
                @ApiResponse(
                        responseCode = "4xx",
                        description = "클라이언트 에러 (잘못된 요청)",
                        content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                @ExampleObject(
                                        name = "Fail - Bad Request",
                                        value =
                                                """
                                                        {
                                                        "status": "fail",
                                                        "data": {
                                                        	"code": "C001",
                                                        	"message": "잘못된 입력값입니다",
                                                        	"detail": {}
                                                        }
                                                        }
                                                        """))),
                @ApiResponse(
                        responseCode = "5xx",
                        description = "서버 에러",
                        content =
                        @Content(
                                mediaType = "application/json",
                                examples =
                                @ExampleObject(
                                        name = "Error - Internal Server Error",
                                        value =
                                                """
                                                        {
                                                        "status": "error",
                                                        "data": {
                                                        	"code": "C002",
                                                        	"message": "서버 오류가 발생했습니다",
                                                        	"detail": null
                                                        }
                                                        }
                                                """)))
        })
public @interface SwaggerResponse {

}
