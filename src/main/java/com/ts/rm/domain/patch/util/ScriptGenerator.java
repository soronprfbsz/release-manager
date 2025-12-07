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
     * @param fromVersion      From 버전
     * @param toVersion        To 버전
     * @param versions         버전 리스트
     * @param files            SQL 파일 리스트
     * @param outputDirPath    출력 디렉토리 경로
     * @param defaultPatchedBy 패치 담당자 기본값 (nullable)
     */
    void generatePatchScript(
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
}
