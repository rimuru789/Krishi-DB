package com.krishidb.krishi_api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.krishidb.krishi_api.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Map<String, Object>> getNotifications() {
        return notificationService.getNotifications();
    }
}
