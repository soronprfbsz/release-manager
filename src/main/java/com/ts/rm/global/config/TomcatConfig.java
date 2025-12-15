package com.ts.rm.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Embedded Tomcat 설정
 * - 대용량 파일 업로드를 위한 POST 요청 크기 제한 해제
 */
@Slf4j
@Configuration
public class TomcatConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addConnectorCustomizers(connector -> {
            // POST 요청 최대 크기 제한 해제 (-1 = unlimited)
            connector.setMaxPostSize(-1);

            // 요청 본문의 최대 크기 제한 해제 (-1 = unlimited)
            // ABORTED 요청 처리 시 버퍼에 저장할 최대 바이트 수
            connector.setMaxSwallowSize(-1);

            log.info("Tomcat Connector configured: maxPostSize=-1 (unlimited), maxSwallowSize=-1 (unlimited)");
        });
    }
}
