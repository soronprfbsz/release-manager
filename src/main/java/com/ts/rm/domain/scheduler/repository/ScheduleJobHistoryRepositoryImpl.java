package com.ts.rm.domain.scheduler.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ts.rm.domain.scheduler.entity.QScheduleJobHistory;
import com.ts.rm.domain.scheduler.enums.JobExecutionStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * ScheduleJobHistory Repository Custom Implementation
 *
 * <p>QueryDSL을 사용한 커스텀 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class ScheduleJobHistoryRepositoryImpl implements ScheduleJobHistoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QScheduleJobHistory history = QScheduleJobHistory.scheduleJobHistory;

    @Override
    public long deleteByStartedAtBefore(LocalDateTime cutoffDate) {
        return queryFactory
                .delete(history)
                .where(history.startedAt.lt(cutoffDate))
                .execute();
    }

    @Override
    public Map<JobExecutionStatus, Long> countByJobIdAndStatusSince(Long jobId, LocalDateTime since) {
        List<Tuple> results = queryFactory
                .select(history.status, history.count())
                .from(history)
                .where(
                        history.job.jobId.eq(jobId),
                        history.startedAt.goe(since)
                )
                .groupBy(history.status)
                .fetch();

        Map<JobExecutionStatus, Long> statusCounts = new HashMap<>();
        for (Tuple tuple : results) {
            JobExecutionStatus status = tuple.get(history.status);
            Long count = tuple.get(history.count());
            statusCounts.put(status, count);
        }
        return statusCounts;
    }
}
