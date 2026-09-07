package com.krishidb.krishi_api.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public Map<String, Object> getSalesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return reportService.getSalesReport(startDate, endDate);
    }

    @GetMapping("/purchases")
    public Map<String, Object> getPurchasesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return reportService.getPurchasesReport(startDate, endDate);
    }

    @GetMapping("/expenses")
    public Map<String, Object> getExpensesReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return reportService.getExpensesReport(startDate, endDate);
    }

    @GetMapping("/inventory")
    public Map<String, Object> getInventoryReport() {
        return reportService.getInventoryReport();
    }
}
