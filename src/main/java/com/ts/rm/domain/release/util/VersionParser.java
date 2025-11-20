package com.ts.rm.domain.release.util;

import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * 버전 파싱 유틸리티
 *
 * <p>버전 문자열(예: 1.1.0)을 파싱하여 major, minor, patch 버전으로 분리
 */
public class VersionParser {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");

    /**
     * 버전 문자열 파싱
     *
     * @param version 버전 문자열 (예: 1.1.0)
     * @return 파싱된 버전 정보
     * @throws BusinessException 잘못된 버전 형식인 경우
     */
    public static VersionInfo parse(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);

        if (!matcher.matches()) {
            throw new BusinessException(ErrorCode.INVALID_VERSION_FORMAT);
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = Integer.parseInt(matcher.group(3));
        String majorMinor = major + "." + minor + ".x";

        return new VersionInfo(major, minor, patch, majorMinor);
    }

    /**
     * 버전 정보 DTO
     */
    @Getter
    public static class VersionInfo {
        private final int majorVersion;
        private final int minorVersion;
        private final int patchVersion;
        private final String majorMinor;

        public VersionInfo(int majorVersion, int minorVersion, int patchVersion,
                String majorMinor) {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.patchVersion = patchVersion;
            this.majorMinor = majorMinor;
        }
    }
}
