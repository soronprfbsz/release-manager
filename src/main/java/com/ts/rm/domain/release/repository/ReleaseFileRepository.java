package com.ts.rm.domain.release.repository;

import com.ts.rm.domain.release.entity.ReleaseFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ReleaseFile Repository
 *
 * <p>릴리즈 파일 조회 및 관리를 위한 Repository
 */
public interface ReleaseFileRepository extends JpaRepository<ReleaseFile, Long>,
        ReleaseFileRepositoryCustom {

    /**
     * 릴리즈 버전 ID로 릴리즈 파일 목록 조회 (실행 순서 오름차순)
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @return 릴리즈 파일 목록
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId ORDER BY rf.executionOrder ASC")
    List<ReleaseFile> findAllByReleaseVersionIdOrderByExecutionOrderAsc(
            @Param("releaseVersionId") Long releaseVersionId);

    /**
     * 릴리즈 버전 ID와 데이터베이스 타입으로 릴리즈 파일 목록 조회
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param databaseType     데이터베이스 타입
     * @return 릴리즈 파일 목록
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.databaseType = :databaseType ORDER BY rf.executionOrder ASC")
    List<ReleaseFile> findByVersionAndDatabaseType(
            @Param("releaseVersionId") Long releaseVersionId,
            @Param("databaseType") String databaseType);

    /**
     * 파일명으로 릴리즈 파일 조회
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param fileName         파일명
     * @return ReleaseFile
     */
    @Query("SELECT rf FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileName = :fileName")
    Optional<ReleaseFile> findByReleaseVersionIdAndFileName(
            @Param("releaseVersionId") Long releaseVersionId,
            @Param("fileName") String fileName);

    /**
     * 파일명 존재 여부 확인
     *
     * @param releaseVersionId 릴리즈 버전 ID
     * @param fileName         파일명
     * @return 존재 여부
     */
    @Query("SELECT CASE WHEN COUNT(rf) > 0 THEN true ELSE false END FROM ReleaseFile rf WHERE rf.releaseVersion.releaseVersionId = :releaseVersionId AND rf.fileName = :fileName")
    boolean existsByReleaseVersionIdAndFileName(@Param("releaseVersionId") Long releaseVersionId,
            @Param("fileName") String fileName);

    /**
     * 파일 경로로 릴리즈 파일 조회
     *
     * @param filePath 파일 경로
     * @return ReleaseFile
     */
    Optional<ReleaseFile> findByFilePath(String filePath);

    /**
     * 버전 범위 내 릴리즈 파일 목록 조회 (데이터베이스 타입별)
     *
     * @param fromVersion  시작 버전
     * @param toVersion    종료 버전
     * @param databaseType 데이터베이스 타입
     * @return 릴리즈 파일 목록
     */
    @Query("SELECT rf FROM ReleaseFile rf " +
            "WHERE rf.releaseVersion.version >= :fromVersion " +
            "AND rf.releaseVersion.version <= :toVersion " +
            "AND rf.databaseType = :databaseType " +
            "ORDER BY rf.releaseVersion.version ASC, rf.executionOrder ASC")
    List<ReleaseFile> findReleaseFilesBetweenVersions(
            @Param("fromVersion") String fromVersion,
            @Param("toVersion") String toVersion,
            @Param("databaseType") String databaseType);
}
