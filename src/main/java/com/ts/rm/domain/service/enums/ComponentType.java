package com.ts.rm.domain.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 서비스 컴포넌트 타입 (접속 정보 유형)
 *
 * <p>서비스를 구성하는 각 컴포넌트의 접속 정보 유형을 정의합니다.
 */
@Getter
@RequiredArgsConstructor
public enum ComponentType {

    /**
     * 웹 접속 정보
     */
    WEB("WEB", "웹"),

    /**
     * 데이터베이스 접속 정보
     */
    DATABASE("DATABASE", "데이터베이스"),

    /**
     * 엔진 접속 정보
     */
    ENGINE("ENGINE", "엔진"),

    /**
     * 기타 접속 정보
     */
    ETC("ETC", "기타");

    /**
     * 코드 ID (COMPONENT_TYPE 코드와 매핑)
     */
    private final String code;

    /**
     * 타입명
     */
    private final String displayName;

    /**
     * 코드로부터 ComponentType 조회
     *
     * @param code 코드 ID
     * @return ComponentType
     * @throws IllegalArgumentException 유효하지 않은 코드인 경우
     */
    public static ComponentType fromCode(String code) {
        for (ComponentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("유효하지 않은 컴포넌트 타입 코드입니다: " + code);
    }
}
