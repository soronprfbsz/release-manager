package com.ts.rm.domain.patch.util;

import com.ts.rm.domain.releasefile.entity.ReleaseFile;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import java.util.List;

/**
 * 패치 실행 스크립트 생성 인터페이스
 *
 * <p>전략 패턴을 사용하여 데이터베이스 타입별 스크립트 생성 전략을 구현합니다.
 */
public interface ScriptGenerator {

    /**
     * 패치 스크립트 생성
     *
     * @param projectId        프로젝트 ID
     * @param fromVersion      From 버전
     * @param toVersion        To 버전
     * @param versions         버전 리스트
     * @param files            SQL 파일 리스트
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (nullable)
     */
    void generatePatchScript(
            String projectId,
            String fromVersion,
            String toVersion,
            List<ReleaseVersion> versions,
            List<ReleaseFile> files,
            String outputDirPath,
            String defaultPatchedBy);

    /**
     * 스크립트 파일명 반환
     *
     * @return 생성될 스크립트 파일명 (예: mariadb_patch.sh)
     */
    String getScriptFileName();

    /**
     * 핫픽스 패치 스크립트 생성
     *
     * <p>핫픽스는 단일 버전에 대한 패치이므로 From-To 범위가 아닌 단일 버전 스크립트를 생성합니다.
     *
     * @param projectId        프로젝트 ID
     * @param hotfixVersion    핫픽스 버전 엔티티
     * @param files            SQL 파일 리스트
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (nullable)
     */
    void generateHotfixScript(
            String projectId,
            ReleaseVersion hotfixVersion,
            List<ReleaseFile> files,
            String outputDirPath,
            String defaultPatchedBy);
}
