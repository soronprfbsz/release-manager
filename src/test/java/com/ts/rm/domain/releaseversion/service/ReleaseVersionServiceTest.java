package com.ts.rm.domain.releaseversion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.ts.rm.domain.customer.entity.Customer;
import com.ts.rm.domain.customer.repository.CustomerRepository;
import com.ts.rm.domain.releaseversion.dto.ReleaseVersionDto;
import com.ts.rm.domain.releaseversion.entity.ReleaseVersion;
import com.ts.rm.domain.releaseversion.enums.ReleaseCategory;
import com.ts.rm.domain.releaseversion.mapper.ReleaseVersionDtoMapper;
import com.ts.rm.domain.releasefile.repository.ReleaseFileRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionHierarchyRepository;
import com.ts.rm.domain.releaseversion.repository.ReleaseVersionRepository;
import com.ts.rm.domain.releaseversion.util.ReleaseMetadataManager;
import com.ts.rm.global.exception.BusinessException;
import com.ts.rm.global.exception.ErrorCode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ReleaseVersion Service 단위 테스트 (TDD)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReleaseVersionService 테스트")
class ReleaseVersionServiceTest {

    @Mock
    private ReleaseVersionRepository releaseVersionRepository;

    @Mock
    private ReleaseFileRepository releaseFileRepository;

    @Mock
    private ReleaseVersionHierarchyRepository hierarchyRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ReleaseVersionDtoMapper mapper;

    @Mock
    private ReleaseMetadataManager metadataManager;

    @Mock
    private ReleaseVersionFileSystemService fileSystemService;

    @Mock
    private ReleaseVersionTreeService treeService;

    @InjectMocks
    private ReleaseVersionService releaseVersionService;

    private Customer testCustomer;
    private ReleaseVersion testVersion;
    private ReleaseVersionDto.CreateRequest createRequest;
    private ReleaseVersionDto.DetailResponse detailResponse;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .customerId(1L)
                .customerCode("company_a")
                .customerName("A회사")
                .isActive(true)
                .createdBy("admin@tscientific")
                .updatedBy("admin@tscientific")
                .build();

        testVersion = ReleaseVersion.builder()
                .releaseVersionId(1L)
                .releaseType("STANDARD")
                .version("1.1.0")
                .majorVersion(1)
                .minorVersion(1)
                .patchVersion(0)
                .createdBy("jhlee@tscientific")
                .comment("새로운 기능")
                .releaseFiles(new ArrayList<>())
                .build();

        createRequest = ReleaseVersionDto.CreateRequest.builder()
                .version("1.1.0")
                .createdBy("jhlee@tscientific")
                .comment("새로운 기능")
                .build();

        detailResponse = new ReleaseVersionDto.DetailResponse(
                1L,
                "infraeye2",
                "Infraeye 2",
                "STANDARD",
                ReleaseCategory.PATCH,
                null,
                "1.1.0",
                1,
                1,
                0,
                "1.1.x",
                "jhlee@tscientific",
                "새로운 기능",
                null,
                LocalDateTime.now(),
                new ArrayList<>()
        );
    }

    @Test
    @DisplayName("표준 릴리즈 버전 생성 - 성공")
    void createStandardVersion_Success() {
        // given
        given(releaseVersionRepository.existsByVersion(anyString())).willReturn(false);
        given(releaseVersionRepository.save(any(ReleaseVersion.class))).willReturn(testVersion);
        given(mapper.toDetailResponse(any(ReleaseVersion.class))).willReturn(detailResponse);

        // when
        ReleaseVersionDto.DetailResponse result = releaseVersionService.createStandardVersion(
                createRequest);

        // then
        assertThat(result).isNotNull();
        assertThat(result.version()).isEqualTo("1.1.0");
        assertThat(result.majorVersion()).isEqualTo(1);
        assertThat(result.minorVersion()).isEqualTo(1);
        assertThat(result.patchVersion()).isEqualTo(0);
        assertThat(result.majorMinor()).isEqualTo("1.1.x");

        then(releaseVersionRepository).should(times(1)).save(any(ReleaseVersion.class));
    }

    @Test
    @DisplayName("표준 릴리즈 버전 생성 - 중복 버전 실패")
    void createStandardVersion_DuplicateVersion() {
        // given
        given(releaseVersionRepository.existsByVersion(anyString())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> releaseVersionService.createStandardVersion(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RELEASE_VERSION_CONFLICT);

        then(releaseVersionRepository).should(never()).save(any(ReleaseVersion.class));
    }

    @Test
    @DisplayName("표준 릴리즈 버전 생성 - 잘못된 버전 형식")
    void createStandardVersion_InvalidVersionFormat() {
        // given
        ReleaseVersionDto.CreateRequest invalidRequest = ReleaseVersionDto.CreateRequest.builder()
                .version("invalid")
                .createdBy("jhlee@tscientific")
                .comment("테스트")
                .build();

        // when & then
        assertThatThrownBy(() -> releaseVersionService.createStandardVersion(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_VERSION_FORMAT);
    }

    @Test
    @DisplayName("커스텀 릴리즈 버전 생성 - 성공")
    void createCustomVersion_Success() {
        // given
        ReleaseVersionDto.CreateRequest customRequest = ReleaseVersionDto.CreateRequest.builder()
                .version("1.0.0")
                .createdBy("admin@tscientific")
                .comment("커스텀 버전")
                .customerId(1L)
                .customVersion("1.0.0-company_a")
                .build();

        ReleaseVersion customVersion = ReleaseVersion.builder()
                .releaseVersionId(2L)
                .releaseType("CUSTOM")
                .customer(testCustomer)
                .version("1.0.0")
                .majorVersion(1)
                .minorVersion(0)
                .patchVersion(0)
                .createdBy("admin@tscientific")
                .comment("커스텀 버전")
                .customVersion("1.0.0-company_a")
                .releaseFiles(new ArrayList<>())
                .build();

        given(customerRepository.findById(anyLong())).willReturn(Optional.of(testCustomer));
        given(releaseVersionRepository.existsByVersion(anyString())).willReturn(false);
        given(releaseVersionRepository.save(any(ReleaseVersion.class))).willReturn(customVersion);
        given(mapper.toDetailResponse(any(ReleaseVersion.class))).willReturn(detailResponse);

        // when
        ReleaseVersionDto.DetailResponse result = releaseVersionService.createCustomVersion(
                customRequest);

        // then
        assertThat(result).isNotNull();

        then(customerRepository).should(times(1)).findById(1L);
        then(releaseVersionRepository).should(times(1)).save(any(ReleaseVersion.class));
    }

    @Test
    @DisplayName("커스텀 릴리즈 버전 생성 - 고객사 ID 없음")
    void createCustomVersion_MissingCustomerId() {
        // given
        ReleaseVersionDto.CreateRequest invalidRequest = ReleaseVersionDto.CreateRequest.builder()
                .version("1.0.0")
                .createdBy("admin@tscientific")
                .comment("커스텀 버전")
                .customerId(null)
                .build();

        // when & then
        assertThatThrownBy(() -> releaseVersionService.createCustomVersion(invalidRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CUSTOMER_ID_REQUIRED);
    }

    @Test
    @DisplayName("릴리즈 버전 조회 (ID) - 성공")
    void getVersionById_Success() {
        // given
        given(releaseVersionRepository.findById(anyLong())).willReturn(Optional.of(testVersion));
        given(mapper.toDetailResponse(any(ReleaseVersion.class))).willReturn(detailResponse);

        // when
        ReleaseVersionDto.DetailResponse result = releaseVersionService.getVersionById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.releaseVersionId()).isEqualTo(1L);

        then(releaseVersionRepository).should(times(1)).findById(1L);
    }

    @Test
    @DisplayName("릴리즈 버전 조회 (ID) - 존재하지 않음")
    void getVersionById_NotFound() {
        // given
        given(releaseVersionRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> releaseVersionService.getVersionById(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RELEASE_VERSION_NOT_FOUND);
    }

    @Test
    @DisplayName("타입별 버전 목록 조회 - 성공")
    void getVersionsByType_Success() {
        // given
        List<ReleaseVersion> versions = List.of(testVersion);
        List<ReleaseVersionDto.SimpleResponse> simpleResponses = List.of(
                new ReleaseVersionDto.SimpleResponse(
                        1L, "infraeye2", "STANDARD", null, "1.1.0", "1.1.x",
                        "jhlee@tscientific", "새로운 기능", new ArrayList<>(),
                        LocalDateTime.now(), 0
                )
        );

        given(releaseVersionRepository.findAllByReleaseTypeOrderByCreatedAtDesc(anyString()))
                .willReturn(versions);
        given(mapper.toSimpleResponseList(any())).willReturn(simpleResponses);

        // when
        List<ReleaseVersionDto.SimpleResponse> result = releaseVersionService.getVersionsByType(
                "STANDARD");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).version()).isEqualTo("1.1.0");
    }

    @Test
    @DisplayName("버전 수정 - 성공")
    void updateVersion_Success() {
        // given
        ReleaseVersionDto.UpdateRequest updateRequest = ReleaseVersionDto.UpdateRequest.builder()
                .comment("수정된 코멘트")
                .build();

        given(releaseVersionRepository.findById(anyLong())).willReturn(Optional.of(testVersion));
        given(mapper.toDetailResponse(any(ReleaseVersion.class))).willReturn(detailResponse);

        // when
        ReleaseVersionDto.DetailResponse result = releaseVersionService.updateVersion(1L,
                updateRequest);

        // then
        assertThat(result).isNotNull();
        // JPA Dirty Checking 사용 - 엔티티 조회만 검증
        then(releaseVersionRepository).should(times(1)).findById(1L);
        then(mapper).should(times(1)).toDetailResponse(any(ReleaseVersion.class));
    }

    @Test
    @DisplayName("버전 삭제 - 성공")
    void deleteVersion_Success() {
        // given
        given(releaseVersionRepository.findById(anyLong())).willReturn(Optional.of(testVersion));

        // when
        releaseVersionService.deleteVersion(1L);

        // then
        then(releaseVersionRepository).should(times(1)).delete(any(ReleaseVersion.class));
    }

    @Test
    @DisplayName("버전 범위 조회 - 성공")
    void getVersionsBetween_Success() {
        // given
        List<ReleaseVersion> versions = List.of(testVersion);
        List<ReleaseVersionDto.SimpleResponse> simpleResponses = List.of(
                new ReleaseVersionDto.SimpleResponse(
                        1L, "infraeye2", "STANDARD", null, "1.1.0", "1.1.x",
                        "jhlee@tscientific", "새로운 기능", new ArrayList<>(),
                        LocalDateTime.now(), 0
                )
        );

        given(releaseVersionRepository.findVersionsBetween(anyString(), anyString(), anyString()))
                .willReturn(versions);
        given(mapper.toSimpleResponseList(any())).willReturn(simpleResponses);

        // when
        List<ReleaseVersionDto.SimpleResponse> result = releaseVersionService.getVersionsBetween(
                "STANDARD", "1.0.0", "1.1.0");

        // then
        assertThat(result).hasSize(1);
    }
}
