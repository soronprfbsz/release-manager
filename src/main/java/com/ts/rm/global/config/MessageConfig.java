package com.ts.rm.global.config;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * 국제화(i18n) 설정 - messages_kr.properties: 한국어 - messages_en.properties: 영어 -
 * Accept-Language 헤더로 언어 자동 선택
 */
@Configuration
public class MessageConfig {

  /**
   * 메시지 소스 설정
   */
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages"); // messages_{locale}.properties
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setDefaultLocale(new Locale("kr")); // 기본 언어: 한국어
    messageSource.setUseCodeAsDefaultMessage(true); // 메시지 키 없으면 키를 반환
    return messageSource;
  }

  /**
   * Locale Resolver 설정 - Accept-Language 헤더를 사용하여 Locale 결정
   */
  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setDefaultLocale(new Locale("kr")); // 기본 언어: 한국어
    return resolver;
  }
}
