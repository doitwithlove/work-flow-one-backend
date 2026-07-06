package com.touchmind.work.flow.one.superuser.controller;

import com.touchmind.work.flow.one.superuser.dto.SuperUserDashboardResponse;
import com.touchmind.work.flow.one.superuser.service.SuperUserDashboardService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/super-user/dashboard")
public class SuperUserDashboardController {

    private final SuperUserDashboardService dashboardService;

    public SuperUserDashboardController(SuperUserDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_USER', 'ADMIN')")
    public Mono<SuperUserDashboardResponse> dashboard() {
        return dashboardService.dashboard();
    }
}
