package com.ts.rm.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // JWT 인증 스킴 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 보안 요구사항 정의
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearer-jwt");

        return new OpenAPI()
                .info(apiInfo())
                .servers(
                        List.of(
                                // 상대 경로 사용 (현재 접속 중인 호스트/포트 자동 사용)
                                new Server()
                                        .url("/")
                                        .description("Current Server")))
                // JWT 인증 스킴 등록
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", securityScheme))
                // 전역 보안 요구사항 적용
                .addSecurityItem(securityRequirement);
    }

    /**
     * 태그 순서를 정의하는 커스터마이저
     */
    @Bean
    public OpenApiCustomizer openApiCustomizer() {
        return openApi -> {
            // 원하는 순서대로 태그 재정렬
            List<Tag> orderedTags = List.of(
                    new Tag().name("인증").description("회원가입, 로그인, 토큰 갱신 API"),
                    new Tag().name("계정").description("계정 관리 API"),
                    new Tag().name("기본").description("공통 코드, 메뉴 등 솔루션 기본 API"),
                    new Tag().name("릴리즈 버전").description("릴리즈 버전 관리 API"),
                    new Tag().name("릴리즈 파일").description("릴리즈 파일 관리 API"),
                    new Tag().name("패치").description("패치 생성 및 조회 API"),
                    new Tag().name("고객사").description("고객사 관리 API"),
                    new Tag().name("엔지니어").description("엔지니어 관리 API"),
                    new Tag().name("부서").description("부서 관리 API"),
                    new Tag().name("리소스 파일").description("리소스 파일 관리 API"),
                    new Tag().name("작업").description("작업 관리 API"),
                    new Tag().name("대시보드").description("대시보드 API"),
                    new Tag().name("프로젝트").description("프로젝트 관리 API"),
                    new Tag().name("데이터 분석").description("데이터 분석 API")
            );
            openApi.setTags(orderedTags);
        };
    }

    private Info apiInfo() {
        return new Info()
                .title("Release Manager Documentation")
                .description("Release Manager REST API 문서")
                .version("v0.0.1")
                .contact(new Contact().name("Tscientific Dev#2 Team")
                        .email("jhlee@tscientific.co.kr"));
    }
}
