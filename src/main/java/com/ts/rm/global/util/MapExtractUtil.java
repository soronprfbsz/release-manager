package com.ts.rm.global.util;

import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Map에서 값을 추출하는 유틸리티 클래스
 *
 * <p>FileSyncAdapter 등에서 additionalData Map에서 값을 추출할 때 사용합니다.
 *
 * <p>사용 예시:
 * <pre>{@code
 * String name = MapExtractUtil.extractString(data, "name");
 * Long id = MapExtractUtil.extractLong(data, "id");
 * Integer order = MapExtractUtil.extractIntegerOrDefault(data, "order", 99);
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MapExtractUtil {

    /**
     * Map에서 String 값 추출
     *
     * @param data 데이터 Map
     * @param key  추출할 키
     * @return 문자열 값 또는 null
     */
    public static String extractString(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Map에서 String 값 추출 (기본값 지정)
     *
     * @param data         데이터 Map
     * @param key          추출할 키
     * @param defaultValue 기본값
     * @return 문자열 값 또는 기본값
     */
    public static String extractStringOrDefault(Map<String, Object> data, String key, String defaultValue) {
        String value = extractString(data, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Map에서 Long 값 추출
     *
     * @param data 데이터 Map
     * @param key  추출할 키
     * @return Long 값 또는 null
     */
    public static Long extractLong(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Map에서 Long 값 추출 (기본값 지정)
     *
     * @param data         데이터 Map
     * @param key          추출할 키
     * @param defaultValue 기본값
     * @return Long 값 또는 기본값
     */
    public static Long extractLongOrDefault(Map<String, Object> data, String key, Long defaultValue) {
        Long value = extractLong(data, key);
        return value != null ? value : defaultValue;
    }

    /**
     * Map에서 Integer 값 추출
     *
     * @param data 데이터 Map
     * @param key  추출할 키
     * @return Integer 값 또는 null
     */
    public static Integer extractInteger(Map<String, Object> data, String key) {
        if (data == null || !data.containsKey(key)) {
            return null;
        }
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Map에서 Integer 값 추출 (기본값 지정)
     *
     * @param data         데이터 Map
     * @param key          추출할 키
     * @param defaultValue 기본값
     * @return Integer 값 또는 기본값
     */
    public static Integer extractIntegerOrDefault(Map<String, Object> data, String key, Integer defaultValue) {
        Integer value = extractInteger(data, key);
        return value != null ? value : defaultValue;
    }
}
