package com.ts.rm.global.file;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * HTTP 파일 다운로드 유틸리티
 *
 * <p>파일 다운로드 시 필요한 HTTP 헤더 생성 및 인코딩 처리를 담당합니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpFileDownloadUtil {

  /**
   * Content-Disposition 헤더 생성 (한글 파일명 지원)
   *
   * <p>RFC 5987 표준을 따라 파일명을 URL 인코딩하여 모든 브라우저에서 호환되도록 처리합니다.
   *
   * <p>지원 브라우저:
   * <ul>
   *   <li>Chrome, Firefox, Edge, Safari - 모두 filename*=UTF-8'' 형식 지원</li>
   *   <li>한글, 일본어, 중국어 등 모든 유니코드 문자 정상 처리</li>
   * </ul>
   *
   * <p>사용 예시:
   * <pre>{@code
   * response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
   *     HttpFileDownloadUtil.buildContentDisposition("테스트파일.zip"));
   * // 결과: "attachment; filename*=UTF-8''%ED%85%8C%EC%8A%A4%ED%8A%B8%ED%8C%8C%EC%9D%BC.zip"
   * }</pre>
   *
   * @param fileName 원본 파일명 (한글 포함 가능)
   * @return RFC 5987 표준에 맞게 인코딩된 Content-Disposition 헤더 값
   */
  public static String buildContentDisposition(String fileName) {
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        .replaceAll("\\+", "%20");  // 공백을 %20으로 변환 (URL 표준)

    // RFC 5987: filename*=UTF-8''encoded-filename
    return String.format("attachment; filename*=UTF-8''%s", encodedFileName);
  }

  /**
   * inline Content-Disposition 헤더 생성 (브라우저에서 바로 열기)
   *
   * <p>파일을 다운로드하지 않고 브라우저에서 바로 표시할 때 사용합니다.
   *
   * @param fileName 원본 파일명
   * @return inline Content-Disposition 헤더 값
   */
  public static String buildInlineContentDisposition(String fileName) {
    String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        .replaceAll("\\+", "%20");

    return String.format("inline; filename*=UTF-8''%s", encodedFileName);
  }
}
