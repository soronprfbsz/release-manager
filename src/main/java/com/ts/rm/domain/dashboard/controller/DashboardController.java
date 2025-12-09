package com.ts.rm.domain.dashboard.controller;

import com.ts.rm.domain.dashboard.dto.DashboardDto;
import com.ts.rm.domain.dashboard.service.DashboardService;
import com.ts.rm.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 대시보드 API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/projects/{projectId}/dashboard")
@RequiredArgsConstructor
public class DashboardController implements DashboardControllerDocs {

    private final DashboardService dashboardService;

    @Override
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<DashboardDto.Response>> getRecentData(
            @PathVariable String projectId,
            @RequestParam(defaultValue = "3") int versionLimit,
            @RequestParam(defaultValue = "3") int patchLimit) {

        DashboardDto.Response response = dashboardService.getRecentData(projectId, versionLimit, patchLimit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
