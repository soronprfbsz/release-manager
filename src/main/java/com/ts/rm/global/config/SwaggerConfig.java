package com.ts.rm.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(
                        List.of(
                                // 상대 경로 사용 (현재 접속 중인 호스트/포트 자동 사용)
                                new Server()
                                        .url("/")
                                        .description("Current Server")));
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
