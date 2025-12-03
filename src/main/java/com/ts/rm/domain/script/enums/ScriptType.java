package com.ts.rm.domain.script.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 스크립트 타입 열거형
 */
@Getter
@RequiredArgsConstructor
public enum ScriptType {

    MARIADB_BACKUP("mariadb-backup", "mariadb_backup.sh", "MariaDB 백업 스크립트", "release/script/MARIADB/mariadb_backup.sh"),
    MARIADB_RESTORE("mariadb-restore", "mariadb_restore.sh", "MariaDB 복원 스크립트", "release/script/MARIADB/mariadb_restore.sh"),
    CRATEDB_BACKUP("cratedb-backup", "cratedb_backup.sh", "CrateDB 스냅샷 백업 스크립트", "release/script/CRATEDB/cratedb_backup.sh"),
    CRATEDB_RESTORE("cratedb-restore", "cratedb_restore.sh", "CrateDB 스냅샷 복원 스크립트", "release/script/CRATEDB/cratedb_restore.sh"),
    INFRAEYE2_INSTALL_GUIDE("infraeye2-install-guide", "Infraeye2 설치가이드(OracleLinux8.6).pdf", "Infraeye2 설치가이드", "release/document/Infraeye2 설치가이드(OracleLinux8.6).pdf");

    private final String code;
    private final String fileName;
    private final String description;
    private final String resourcePath;

    /**
     * 코드로 ScriptType 조회
     */
    public static ScriptType fromCode(String code) {
        for (ScriptType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown script type: " + code);
    }
}
