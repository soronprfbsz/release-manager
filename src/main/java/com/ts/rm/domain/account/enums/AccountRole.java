package com.ts.rm.domain.account.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountRole {
    ADMIN("ADMIN", "관리자"),
    USER("USER", "일반 사용자");

    private final String codeId;
    private final String description;

    public static AccountRole fromCodeId(String codeId) {
        for (AccountRole role : values()) {
            if (role.getCodeId().equals(codeId)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role code: " + codeId);
    }
}
