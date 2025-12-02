package com.ts.rm.domain.releasefile.util;

import com.ts.rm.domain.releasefile.enums.FileCategory;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.util.Map;
import java.util.Set;

/**
 * SubCategory 유효성 검증 유틸리티
 *
 * <p>각 FileCategory에 허용된 subCategory 값을 검증합니다.
 */
public class SubCategoryValidator {

    /**
     * 카테고리별 허용된 서브 카테고리 매핑
     *
     * <p>Database: FILE_SUBCATEGORY_DATABASE (MARIADB, CRATEDB, ETC)
     * <p>Web: FILE_SUBCATEGORY_WEB (BUILD, IMAGE, METADATA, ETC)
     * <p>Install: FILE_SUBCATEGORY_INSTALL (SH, IMAGE, METADATA, ETC)
     * <p>Engine: FILE_SUBCATEGORY_ENGINE (NC_AI_EVENT, NC_AI_LEARN, NC_AP, ..., ETC)
     */
    private static final Map<FileCategory, Set<String>> ALLOWED_SUBCATEGORIES = Map.of(
            FileCategory.DATABASE, Set.of("MARIADB", "CRATEDB", "ETC"),
            FileCategory.WEB, Set.of("BUILD", "IMAGE", "METADATA", "ETC"),
            FileCategory.INSTALL, Set.of("SH", "IMAGE", "METADATA", "ETC"),
            FileCategory.ENGINE, Set.of(
                "NC_AI_EVENT", "NC_AI_LEARN", "NC_AI_MGR", "NC_AP", "NC_API_AP", "NC_API_KAL",
                "NC_ARP", "NC_CONF", "NC_CONFIBACK", "NC_CPPM_CHK", "NC_CUSTOM", "NC_DB_MIG",
                "NC_DPI_KUMOH", "NC_EMS", "NC_EVENT_SENDER", "NC_EVENTPUSHER", "NC_EXEC",
                "NC_FAULT_CP", "NC_FAULT_EX", "NC_FAULT_MS", "NC_FMS", "NC_HTTP_SVR",
                "NC_IPSLA", "NC_IPT", "NC_IPT_CDR", "NC_IPTMAC", "NC_KNB", "NC_L4", "NC_L4LB",
                "NC_L7", "NC_MIB_PARSER", "NC_NAC_KUMOH", "NC_NDI", "NC_NET_SCAN", "NC_NOTI",
                "NC_NOTI_TCPCLIENT", "NC_PACKET", "NC_PERF", "NC_PERF_LEARN", "NC_REPEAT_EVENT",
                "NC_REQUEST_URL", "NC_REQUEST_URL_NOKIA", "NC_REST_API", "NC_RT_TOOL", "NC_RTT_CLI",
                "NC_SAMPLE", "NC_SDN", "NC_SFLOW_C", "NC_SLB", "NC_SMS", "NC_SNMP", "NC_SNMP3_CHK",
                "NC_SVC_CHK", "NC_SYSTRAP", "NC_TMS", "NC_TRACERT", "NC_UI_CP", "NC_UPS",
                "NC_UTIL", "NC_VMM", "NC_VPN", "NC_WATCHDOG", "NC_X25", "OZ_CCTV", "ETC"
            )
    );

    /**
     * 특정 카테고리에서 subCategory가 유효한지 확인
     *
     * @param fileCategory 파일 카테고리
     * @param subCategory  서브 카테고리
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(FileCategory fileCategory, String subCategory) {
        if (fileCategory == null || subCategory == null) {
            return true; // null은 허용
        }

        Set<String> allowedValues = ALLOWED_SUBCATEGORIES.get(fileCategory);
        if (allowedValues == null) {
            return false;
        }

        return allowedValues.contains(subCategory.toLowerCase());
    }

    /**
     * 카테고리에 허용된 모든 서브 카테고리 조회
     *
     * @param fileCategory 파일 카테고리
     * @return 허용된 서브 카테고리 Set
     */
    public static Set<String> getAllowedSubCategories(FileCategory fileCategory) {
        return ALLOWED_SUBCATEGORIES.getOrDefault(fileCategory, Set.of());
    }
}
