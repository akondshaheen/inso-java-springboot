package com.inso.learning.taskflow.dto.response;

/**
 * A small RESPONSE DTO describing the outcome of ONE simulated
 * notification send, returned inside a list by
 * NotificationController's overdue-reminders endpoint. "success" lets the
 * client tell at a glance whether a particular reminder failed, without
 * needing to parse error text.
 */
public record NotificationResult(Long taskId, String recipientEmail, boolean success, String detail) {
}
