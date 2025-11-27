package com.ts.rm.global.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import com.ts.rm.config.TestQueryDslConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * ApiLoggingFilter 통합 테스트
 * - dev 환경에서 필터 활성화 검증
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestQueryDslConfig.class)
class ApiLoggingFilterTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @DisplayName("dev 환경에서는 ApiLoggingFilter 빈이 등록되어야 함")
  void shouldRegisterApiLoggingFilterBeanInDevEnvironment() {
    // Given & When
    boolean hasApiLoggingFilter = applicationContext.containsBean("apiLoggingFilter");

    // Then
    assertThat(hasApiLoggingFilter).isTrue();
  }

  @Test
  @DisplayName("Actuator health 엔드포인트는 정상적으로 동작해야 함")
  void shouldAccessActuatorHealthEndpoint() throws Exception {
    // Given - Actuator health 경로

    // When & Then
    // Actuator health 엔드포인트가 정상적으로 동작해야 함
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk());
  }
}
