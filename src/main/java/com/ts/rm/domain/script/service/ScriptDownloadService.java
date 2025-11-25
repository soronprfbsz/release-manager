package com.ts.rm.domain.script.service;

import com.ts.rm.domain.script.enums.ScriptType;
import com.ts.rm.global.common.exception.BusinessException;
import com.ts.rm.global.common.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * 스크립트 다운로드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScriptDownloadService {

    /**
     * 스크립트 파일 리소스 조회
     *
     * @param scriptType 스크립트 타입
     * @return 스크립트 파일 리소스
     */
    public Resource getScriptResource(ScriptType scriptType) {
        log.info("스크립트 파일 조회 - type: {}, path: {}", scriptType.getCode(),
                scriptType.getResourcePath());

        ClassPathResource resource = new ClassPathResource(scriptType.getResourcePath());

        if (!resource.exists()) {
            log.error("스크립트 파일을 찾을 수 없습니다: {}", scriptType.getResourcePath());
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND,
                    "스크립트 파일을 찾을 수 없습니다: " + scriptType.getFileName());
        }

        return resource;
    }

    /**
     * 스크립트 파일 바이트 배열로 조회
     *
     * @param scriptType 스크립트 타입
     * @return 스크립트 파일 바이트 배열
     */
    public byte[] getScriptBytes(ScriptType scriptType) {
        Resource resource = getScriptResource(scriptType);

        try (InputStream inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            log.error("스크립트 파일 읽기 실패: {}", scriptType.getResourcePath(), e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "스크립트 파일 읽기 실패: " + scriptType.getFileName());
        }
    }

    /**
     * 스크립트 타입 목록 조회
     *
     * @return 스크립트 타입 목록
     */
    public List<Map<String, String>> getScriptTypes() {
        return Arrays.stream(ScriptType.values())
                .map(type -> Map.of(
                        "code", type.getCode(),
                        "fileName", type.getFileName(),
                        "description", type.getDescription()
                ))
                .collect(Collectors.toList());
    }
}
