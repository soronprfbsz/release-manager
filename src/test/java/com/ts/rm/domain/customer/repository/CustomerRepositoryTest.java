package com.ts.rm.domain.customer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.customer.entity.Customer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Customer Repository 단위 테스트
 */
@DataJpaTest
@Import(CustomerRepositoryTest.TestConfig.class)
@ActiveProfiles("test")
@DisplayName("CustomerRepository 테스트")
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .customerCode("company_a")
                .customerName("A회사")
                .description("A회사 설명")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("고객사 저장 - 성공")
    void save_Success() {
        // when
        Customer saved = customerRepository.save(testCustomer);

        // then
        assertThat(saved.getCustomerId()).isNotNull();
        assertThat(saved.getCustomerCode()).isEqualTo("company_a");
        assertThat(saved.getCustomerName()).isEqualTo("A회사");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("고객사 코드로 조회 - 성공")
    void findByCustomerCode_Success() {
        // given
        customerRepository.save(testCustomer);

        // when
        Optional<Customer> found = customerRepository.findByCustomerCode("company_a");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getCustomerCode()).isEqualTo("company_a");
    }

    @Test
    @DisplayName("고객사 코드로 조회 - 없음")
    void findByCustomerCode_NotFound() {
        // when
        Optional<Customer> found = customerRepository.findByCustomerCode("nonexistent");

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("활성화된 고객사 목록 조회 - 성공")
    void findAllByIsActive_Success() {
        // given
        customerRepository.save(testCustomer);

        Customer inactiveCustomer = Customer.builder()
                .customerCode("company_b")
                .customerName("B회사")
                .description("B회사 설명")
                .isActive(false)
                .build();
        customerRepository.save(inactiveCustomer);

        // when
        List<Customer> activeCustomers = customerRepository.findAllByIsActive(true);

        // then
        assertThat(activeCustomers).hasSize(1);
        assertThat(activeCustomers.get(0).getCustomerCode()).isEqualTo("company_a");
    }

    @Test
    @DisplayName("고객사 코드 존재 여부 확인 - 존재함")
    void existsByCustomerCode_True() {
        // given
        customerRepository.save(testCustomer);

        // when
        boolean exists = customerRepository.existsByCustomerCode("company_a");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("고객사 코드 존재 여부 확인 - 존재하지 않음")
    void existsByCustomerCode_False() {
        // when
        boolean exists = customerRepository.existsByCustomerCode("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("고객사명으로 검색 - 성공")
    void findByCustomerNameContaining_Success() {
        // given
        customerRepository.save(testCustomer);

        Customer anotherCustomer = Customer.builder()
                .customerCode("company_b")
                .customerName("홍길동회사")
                .description("설명")
                .isActive(true)
                .build();
        customerRepository.save(anotherCustomer);

        // when
        List<Customer> results = customerRepository.findByCustomerNameContaining("A회사");

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCustomerName()).contains("A회사");
    }

    /**
     * JPA Auditing 및 QueryDSL 설정
     *
     * <p>Customer 도메인은 QueryDSL Custom을 사용하지 않지만,
     * <p>다른 Repository들(ReleaseVersion, ReleaseFile 등)이 아직 Custom Impl을 사용하므로
     * <p>JPAQueryFactory 빈이 필요함
     */
    @org.springframework.boot.test.context.TestConfiguration
    @org.springframework.data.jpa.repository.config.EnableJpaAuditing
    static class TestConfig {
        @org.springframework.context.annotation.Bean
        public com.querydsl.jpa.impl.JPAQueryFactory jpaQueryFactory(
                jakarta.persistence.EntityManager entityManager) {
            return new com.querydsl.jpa.impl.JPAQueryFactory(entityManager);
        }
    }
}
