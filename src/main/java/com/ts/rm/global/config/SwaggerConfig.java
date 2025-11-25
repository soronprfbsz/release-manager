package com.ts.rm.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
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

    private Info apiInfo() {
        return new Info()
                .title("Release Manager Documentation")
                .description("Release Manager REST API 문서")
                .version("v0.0.1")
                .contact(new Contact().name("Tscientific Dev#2 Team")
                        .email("jhlee@tscientific.co.kr"));
    }
}
