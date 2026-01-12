package com.ts.rm.global.security;

import java.util.Collection;
import java.util.Collections;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 확장된 사용자 정보를 담는 커스텀 UserDetails 구현체
 *
 * <p>JWT 토큰에서 추출한 accountId를 기반으로 DB에서 조회한 전체 사용자 정보를 담습니다.
 * SecurityContext에 저장되어 애플리케이션 전반에서 사용됩니다.
 */
@Getter
@Builder
public class AccountUserDetails implements UserDetails {

    private final Long accountId;
    private final String email;
    private final String accountName;
    private final String password;
    private final String role;
    private final Long departmentId;
    private final String departmentName;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Spring Security에서 username으로 사용되는 값
     * accountId를 문자열로 반환
     */
    @Override
    public String getUsername() {
        return String.valueOf(accountId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
