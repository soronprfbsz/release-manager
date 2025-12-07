package com.ts.rm.global.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FileSizeUtil 테스트
 */
class FileSizeUtilTest {

    @Test
    @DisplayName("Bytes 포맷팅 - 1024 미만")
    void formatBytes_lessThan1KB() {
        // given
        long bytes = 512;

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("512 B");
    }

    @Test
    @DisplayName("KB 포맷팅 - 정수")
    void formatBytes_KB_integer() {
        // given
        long bytes = 2048; // 2 KB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("2 KB");
    }

    @Test
    @DisplayName("KB 포맷팅 - 소수점")
    void formatBytes_KB_decimal() {
        // given
        long bytes = 1536; // 1.5 KB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("1.5 KB");
    }

    @Test
    @DisplayName("MB 포맷팅 - 정수")
    void formatBytes_MB_integer() {
        // given
        long bytes = 3145728; // 3 MB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("3 MB");
    }

    @Test
    @DisplayName("MB 포맷팅 - 소수점")
    void formatBytes_MB_decimal() {
        // given
        long bytes = 2621440; // 2.5 MB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("2.5 MB");
    }

    @Test
    @DisplayName("GB 포맷팅 - 정수")
    void formatBytes_GB_integer() {
        // given
        long bytes = 5368709120L; // 5 GB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("5 GB");
    }

    @Test
    @DisplayName("GB 포맷팅 - 소수점")
    void formatBytes_GB_decimal() {
        // given
        long bytes = 4026531840L; // 3.75 GB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("3.8 GB"); // 반올림
    }

    @Test
    @DisplayName("TB 포맷팅 - 정수")
    void formatBytes_TB_integer() {
        // given
        long bytes = 2199023255552L; // 2 TB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("2 TB");
    }

    @Test
    @DisplayName("0 bytes 포맷팅")
    void formatBytes_zero() {
        // given
        long bytes = 0;

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("0 B");
    }

    @Test
    @DisplayName("음수 입력 시 예외 발생")
    void formatBytes_negativeValue_throwsException() {
        // given
        long bytes = -1024;

        // when & then
        assertThatThrownBy(() -> FileSizeUtil.formatBytes(bytes))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일 크기는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("경계값 테스트 - 1 KB")
    void formatBytes_boundary_1KB() {
        // given
        long bytes = 1024; // 정확히 1 KB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("1 KB");
    }

    @Test
    @DisplayName("경계값 테스트 - 1 MB")
    void formatBytes_boundary_1MB() {
        // given
        long bytes = 1048576; // 정확히 1 MB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("1 MB");
    }

    @Test
    @DisplayName("경계값 테스트 - 1 GB")
    void formatBytes_boundary_1GB() {
        // given
        long bytes = 1073741824; // 정확히 1 GB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("1 GB");
    }

    @Test
    @DisplayName("실제 파일 크기 예시 - 작은 파일")
    void formatBytes_realWorldExample_smallFile() {
        // given
        long bytes = 15728; // 약 15.4 KB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("15.4 KB");
    }

    @Test
    @DisplayName("실제 파일 크기 예시 - 중간 파일")
    void formatBytes_realWorldExample_mediumFile() {
        // given
        long bytes = 52428800; // 50 MB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("50 MB");
    }

    @Test
    @DisplayName("실제 파일 크기 예시 - 큰 파일")
    void formatBytes_realWorldExample_largeFile() {
        // given
        long bytes = 7516192768L; // 약 7 GB

        // when
        String result = FileSizeUtil.formatBytes(bytes);

        // then
        assertThat(result).isEqualTo("7 GB");
    }
}
