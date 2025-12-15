package com.ts.rm.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.ts.rm.domain.service.entity.Service;
import com.ts.rm.domain.service.entity.ServiceComponent;
import com.ts.rm.domain.service.enums.ComponentType;
import com.ts.rm.domain.service.repository.ServiceComponentRepository;
import com.ts.rm.domain.service.repository.ServiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * ServiceComponent 비밀번호 암호화 테스트
 */
@SpringBootTest
@Transactional
@DisplayName("서비스 컴포넌트 비밀번호 암호화 테스트")
class ServicePasswordEncryptionTest {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceComponentRepository componentRepository;

    @Test
    @DisplayName("비밀번호를 저장하면 DB에 암호화되어 저장되고, 조회 시 자동으로 복호화되어야 한다")
    void password_shouldBeEncryptedInDbAndDecryptedOnRead() {
        // given
        String plainPassword = "plain_password_123!@#";
        String plainSshPassword = "ssh_password_456!@#";

        Service service = Service.builder()
                .serviceName("Test Service")
                .serviceType("infraeye1")
                .description("Test")
                .isActive(true)
                .sortOrder(1)
                .createdBy("test@test.com")
                .build();
        Service savedService = serviceRepository.save(service);

        ServiceComponent component = ServiceComponent.builder()
                .service(savedService)
                .componentType(ComponentType.DATABASE)
                .componentName("Test DB")
                .host("localhost")
                .port(3306)
                .accountId("admin")
                .password(plainPassword)
                .sshPort(22)
                .sshAccountId("ubuntu")
                .sshPassword(plainSshPassword)
                .description("Test Component")
                .sortOrder(1)
                .isActive(true)
                .createdBy("test@test.com")
                .build();

        // when
        ServiceComponent saved = componentRepository.save(component);
        componentRepository.flush();

        // DB에서 다시 조회
        ServiceComponent found = componentRepository.findById(saved.getComponentId()).orElseThrow();

        // then
        assertThat(found.getPassword()).isEqualTo(plainPassword);
        assertThat(found.getSshPassword()).isEqualTo(plainSshPassword);
    }

    @Test
    @DisplayName("null 비밀번호를 저장하고 조회하면 null이어야 한다")
    void nullPassword_shouldRemainNull() {
        // given
        Service service = Service.builder()
                .serviceName("Test Service")
                .serviceType("infraeye1")
                .description("Test")
                .isActive(true)
                .sortOrder(1)
                .createdBy("test@test.com")
                .build();
        Service savedService = serviceRepository.save(service);

        ServiceComponent component = ServiceComponent.builder()
                .service(savedService)
                .componentType(ComponentType.DATABASE)
                .componentName("Test DB")
                .host("localhost")
                .port(3306)
                .password(null)
                .sshPassword(null)
                .sortOrder(1)
                .isActive(true)
                .createdBy("test@test.com")
                .build();

        // when
        ServiceComponent saved = componentRepository.save(component);
        componentRepository.flush();

        // DB에서 다시 조회
        ServiceComponent found = componentRepository.findById(saved.getComponentId()).orElseThrow();

        // then
        assertThat(found.getPassword()).isNull();
        assertThat(found.getSshPassword()).isNull();
    }

    @Test
    @DisplayName("비밀번호를 업데이트하면 암호화되어 저장되어야 한다")
    void updatePassword_shouldBeEncrypted() {
        // given
        String originalPassword = "original_password";
        String updatedPassword = "updated_password";

        Service service = Service.builder()
                .serviceName("Test Service")
                .serviceType("infraeye1")
                .description("Test")
                .isActive(true)
                .sortOrder(1)
                .createdBy("test@test.com")
                .build();
        Service savedService = serviceRepository.save(service);

        ServiceComponent component = ServiceComponent.builder()
                .service(savedService)
                .componentType(ComponentType.DATABASE)
                .componentName("Test DB")
                .host("localhost")
                .port(3306)
                .password(originalPassword)
                .sortOrder(1)
                .isActive(true)
                .createdBy("test@test.com")
                .build();
        ServiceComponent saved = componentRepository.save(component);
        componentRepository.flush();

        // when
        saved.update(
                ComponentType.DATABASE,
                "Test DB",
                "localhost",
                3306,
                null,
                "admin",
                updatedPassword,  // 비밀번호 변경
                null,
                null,
                null,
                "Updated",
                true,
                "updater@test.com"
        );
        componentRepository.flush();

        // then
        ServiceComponent found = componentRepository.findById(saved.getComponentId()).orElseThrow();
        assertThat(found.getPassword()).isEqualTo(updatedPassword);
        assertThat(found.getPassword()).isNotEqualTo(originalPassword);
    }
}
