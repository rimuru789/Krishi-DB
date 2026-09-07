package com.krishidb.krishi_api.controller;

import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.dto.DashboardSummaryDTO;
import com.krishidb.krishi_api.service.DashboardService;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardSummaryDTO getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping
    public DashboardSummaryDTO getDashboardSummary() {
        return dashboardService.getDashboardStats();
    }
}
