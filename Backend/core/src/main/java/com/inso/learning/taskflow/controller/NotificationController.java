package com.inso.learning.taskflow.controller;

import com.inso.learning.taskflow.dto.response.NotificationResult;
import com.inso.learning.taskflow.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes NotificationService's concurrent reminder-sending feature over
 * HTTP. Only an ADMIN may trigger this (see the ".hasRole(ADMIN)" rule for
 * "/api/notifications/**" in SecurityConfig) - sending reminder messages
 * to every assignee is an administrative bulk action, not something an
 * ordinary user should be able to trigger.
 *
 * Notice how thin this controller is: it does not know or care that the
 * work underneath is happening concurrently on a thread pool - that
 * detail is entirely encapsulated inside NotificationService. This is
 * exactly the point of the Controller-Service-Repository layering: the
 * controller's only job is to translate an HTTP request into a service
 * call and the service's result back into an HTTP response.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/overdue-reminders")
    public List<NotificationResult> sendOverdueReminders() {
        return notificationService.sendOverdueTaskReminders();
    }
}
