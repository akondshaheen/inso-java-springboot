package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.*;
import com.inso.learning.taskflow.dto.response.NotificationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * =============================================================================
 * A UNIT TEST FOR CONCURRENT CODE: WHY IS THIS ACTUALLY TRICKY?
 * =============================================================================
 *
 * Testing concurrent code is harder than testing simple business logic
 * because the whole point of NotificationService is to run work on OTHER
 * threads. We are not trying to prove here that Java's ExecutorService or
 * Future work correctly (that is the JDK's own responsibility, already
 * covered by the JDK's own test suite) - we are proving that
 * NotificationService correctly USES them: that every overdue task with
 * an assignee produces exactly one NotificationResult, and that a task
 * with no assignee is safely skipped. Mockito's TaskService mock lets us
 * control exactly which tasks "getOverdueTasks()" returns, keeping this a
 * fast, deterministic unit test with no real waiting, database, or
 * network calls involved.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendsOneReminderPerOverdueTaskThatHasAnAssignee() {
        User assignee = new User("Alice", "alice@example.com", "hashed-password", Role.USER);
        User owner = new User("Bob", "bob@example.com", "hashed-password", Role.USER);
        Project project = new Project("Website Revamp", "Redesign the marketing site", owner);

        Task taskWithAssignee = new Task("Fix login bug", "Users cannot log in", Priority.HIGH,
                LocalDate.now().minusDays(1), project);
        taskWithAssignee.setId(1L);
        taskWithAssignee.setAssignee(assignee);

        Task taskWithoutAssignee = new Task("Write docs", "Update the README", Priority.LOW,
                LocalDate.now().minusDays(2), project);
        taskWithoutAssignee.setId(2L);
        // No assignee set on purpose - this task should be skipped.

        when(taskService.getOverdueTasks()).thenReturn(List.of(taskWithAssignee, taskWithoutAssignee));

        List<NotificationResult> results = notificationService.sendOverdueTaskReminders();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).taskId()).isEqualTo(1L);
        assertThat(results.get(0).recipientEmail()).isEqualTo("alice@example.com");
        assertThat(results.get(0).success()).isTrue();
    }

    @Test
    void returnsEmptyListWhenThereAreNoOverdueTasks() {
        when(taskService.getOverdueTasks()).thenReturn(List.of());

        List<NotificationResult> results = notificationService.sendOverdueTaskReminders();

        assertThat(results).isEmpty();
    }
}
