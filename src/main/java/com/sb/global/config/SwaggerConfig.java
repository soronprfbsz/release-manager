package com.sb.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.host:localhost}")
    private String serverHost;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(
                        List.of(new Server().url(
                                        "http://" + serverHost + ":" + serverPort)
                                .description("API 서버")));
    }

    private Info apiInfo() {
        return new Info()
                .title("Springboot Boilerplate Documentation")
                .description("Springboot Boilerplate REST API 문서")
                .version("v0.0.1")
                .contact(new Contact().name("Springboot Boilerplate Team")
                        .email("support@sb.com"));
    }
}
