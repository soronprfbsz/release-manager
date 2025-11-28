package com.ts.rm.domain.releaseversion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Release Metadata DTO
 *
 * <p>release_metadata.json 파일 구조를 표현하는 DTO
 */
public final class ReleaseMetadataDto {

    private ReleaseMetadataDto() {
    }

    /**
     * 릴리즈 메타데이터 문서 (최상위)
     */
    public record MetadataDocument(
            @JsonProperty("versions")
            List<MetadataEntry> versions
    ) {
        public MetadataDocument {
            if (versions == null) {
                versions = new java.util.ArrayList<>();
            }
        }
    }

    /**
     * 개별 버전 메타데이터 항목
     */
    public record MetadataEntry(
            @JsonProperty("version")
            String version,

            @JsonProperty("createdAt")
            String createdAt,

            @JsonProperty("createdBy")
            String createdBy,

            @JsonProperty("comment")
            String comment
    ) {
    }
}
