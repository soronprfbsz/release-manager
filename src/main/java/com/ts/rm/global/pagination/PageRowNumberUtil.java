package com.ts.rm.global.pagination;

import java.util.List;
import java.util.function.BiFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 페이지네이션 rowNumber 계산 유틸리티
 *
 * <p>페이징된 목록에서 각 항목의 행 번호를 계산합니다.
 * 오름차순으로 1부터 시작하는 행 번호를 반환합니다.
 *
 * <p><b>계산 공식</b>: rowNumber = offset + indexInPage + 1
 *
 * <p><b>예시</b>: 페이지 크기 10, 2페이지(offset=10) → 11, 12, 13...
 *
 * <p><b>사용 예시</b>:
 * <pre>{@code
 * // 기본 사용법
 * Page<MyDto> result = myPage.map(entity -> {
 *     long rowNumber = PageRowNumberUtil.calculateRowNumber(myPage, entity);
 *     return new MyListResponse(rowNumber, entity.getId(), entity.getName());
 * });
 *
 * // mapWithRowNumber 사용법
 * Page<MyListResponse> result = PageRowNumberUtil.mapWithRowNumber(
 *     myPage,
 *     (entity, rowNumber) -> new MyListResponse(rowNumber, entity.getId(), entity.getName())
 * );
 * }</pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageRowNumberUtil {

    /**
     * Page 내 특정 요소의 rowNumber 계산 (오름차순 기준)
     *
     * <p>목록에서 각 항목이 전체 데이터 중 몇 번째인지 계산합니다.
     * 예: 페이지 크기 10, 2페이지(offset=10), 페이지 내 3번째 항목(index=2) → rowNumber = 10 + 2 + 1 = 13
     *
     * @param page    페이지 객체
     * @param element 페이지 내 요소
     * @param <T>     요소 타입
     * @return 행 번호 (1부터 시작, 오름차순)
     */
    public static <T> long calculateRowNumber(Page<T> page, T element) {
        long offset = page.getPageable().getOffset();
        int indexInPage = page.getContent().indexOf(element);

        return offset + indexInPage + 1;
    }

    /**
     * 페이징 정보와 인덱스로 rowNumber 계산 (오름차순 기준)
     *
     * <p>Page 객체 없이 직접 계산할 때 사용합니다.
     *
     * @param pageable    페이징 정보
     * @param indexInPage 페이지 내 인덱스 (0부터 시작)
     * @return 행 번호 (1부터 시작, 오름차순)
     */
    public static long calculateRowNumber(Pageable pageable, int indexInPage) {
        return pageable.getOffset() + indexInPage + 1;
    }

    /**
     * Page를 rowNumber가 포함된 새로운 타입으로 변환
     *
     * <p>각 요소를 rowNumber와 함께 새로운 DTO로 변환합니다.
     *
     * @param page   원본 페이지
     * @param mapper 변환 함수 (원본 요소, rowNumber) → 새로운 DTO
     * @param <T>    원본 타입
     * @param <R>    결과 타입
     * @return 변환된 페이지
     */
    public static <T, R> Page<R> mapWithRowNumber(Page<T> page, BiFunction<T, Long, R> mapper) {
        List<T> content = page.getContent();

        return page.map(element -> {
            long rowNumber = calculateRowNumber(page, element);
            return mapper.apply(element, rowNumber);
        });
    }
}
