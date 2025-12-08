package com.ts.rm.global.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ts.rm.domain.account.enums.AccountRole;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

/**
 * RoleValidator 테스트
 */
@DisplayName("RoleValidator 테스트")
class RoleValidatorTest {

    private MockedStatic<SecurityUtil> securityUtilMock;

    @BeforeEach
    void setUp() {
        securityUtilMock = mockStatic(SecurityUtil.class);
    }

    @AfterEach
    void tearDown() {
        if (securityUtilMock != null) {
            securityUtilMock.close();
        }
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("ADMIN 권한이 있으면 requireAdmin() 성공")
    void requireAdmin_Success_WhenUserIsAdmin() {
        // Given: ADMIN 권한을 가진 TokenInfo
        TokenInfo adminTokenInfo = TokenInfo.builder()
                .email("admin@example.com")
                .role(AccountRole.ADMIN.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(adminTokenInfo);

        // When & Then: 예외 발생하지 않음
        assertDoesNotThrow(() -> RoleValidator.requireAdmin());
    }

    @Test
    @DisplayName("ADMIN 권한이 없으면 requireAdmin() 실패")
    void requireAdmin_Fail_WhenUserIsNotAdmin() {
        // Given: USER 권한을 가진 TokenInfo
        TokenInfo userTokenInfo = TokenInfo.builder()
                .email("user@example.com")
                .role(AccountRole.USER.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(userTokenInfo);

        // When & Then: BusinessException(FORBIDDEN) 발생
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RoleValidator.requireAdmin());

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("role이 null이면 requireAdmin() 실패")
    void requireAdmin_Fail_WhenRoleIsNull() {
        // Given: role이 null인 TokenInfo
        TokenInfo tokenInfo = TokenInfo.builder()
                .email("user@example.com")
                .role(null)
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(tokenInfo);

        // When & Then: BusinessException(FORBIDDEN) 발생
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RoleValidator.requireAdmin());

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("특정 role 검증 - 성공")
    void requireRole_Success() {
        // Given: ADMIN 권한을 가진 TokenInfo
        TokenInfo adminTokenInfo = TokenInfo.builder()
                .email("admin@example.com")
                .role(AccountRole.ADMIN.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(adminTokenInfo);

        // When & Then: 예외 발생하지 않음
        assertDoesNotThrow(() -> RoleValidator.requireRole(AccountRole.ADMIN));
    }

    @Test
    @DisplayName("특정 role 검증 - 실패")
    void requireRole_Fail() {
        // Given: USER 권한을 가진 TokenInfo
        TokenInfo userTokenInfo = TokenInfo.builder()
                .email("user@example.com")
                .role(AccountRole.USER.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(userTokenInfo);

        // When & Then: BusinessException(FORBIDDEN) 발생
        BusinessException exception = assertThrows(BusinessException.class,
                () -> RoleValidator.requireRole(AccountRole.ADMIN));

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("isAdmin() - ADMIN이면 true 반환")
    void isAdmin_ReturnTrue_WhenUserIsAdmin() {
        // Given: ADMIN 권한을 가진 TokenInfo
        TokenInfo adminTokenInfo = TokenInfo.builder()
                .email("admin@example.com")
                .role(AccountRole.ADMIN.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(adminTokenInfo);

        // When
        boolean result = RoleValidator.isAdmin();

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("isAdmin() - USER면 false 반환")
    void isAdmin_ReturnFalse_WhenUserIsNotAdmin() {
        // Given: USER 권한을 가진 TokenInfo
        TokenInfo userTokenInfo = TokenInfo.builder()
                .email("user@example.com")
                .role(AccountRole.USER.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(userTokenInfo);

        // When
        boolean result = RoleValidator.isAdmin();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("isAdmin() - 예외 발생 시 false 반환")
    void isAdmin_ReturnFalse_WhenExceptionOccurs() {
        // Given: SecurityUtil.getTokenInfo()가 예외 발생
        securityUtilMock.when(SecurityUtil::getTokenInfo)
                .thenThrow(new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // When
        boolean result = RoleValidator.isAdmin();

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("getCurrentRole() - 현재 사용자 role 반환")
    void getCurrentRole_ReturnRole() {
        // Given: ADMIN 권한을 가진 TokenInfo
        TokenInfo adminTokenInfo = TokenInfo.builder()
                .email("admin@example.com")
                .role(AccountRole.ADMIN.getCodeId())
                .build();

        securityUtilMock.when(SecurityUtil::getTokenInfo).thenReturn(adminTokenInfo);

        // When
        String role = RoleValidator.getCurrentRole();

        // Then
        assertEquals(AccountRole.ADMIN.getCodeId(), role);
    }
}
