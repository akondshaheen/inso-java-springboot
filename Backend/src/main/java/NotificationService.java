package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.Task;
import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.dto.response.NotificationResult;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * =============================================================================
 * STAGE 10: CONCURRENCY - SENDING REMINDER NOTIFICATIONS IN PARALLEL
 * =============================================================================
 *
 * WHAT PROBLEM DOES THIS SERVICE SOLVE?
 * -------------------------------------------------------------------------
 * When an admin asks TaskFlow to remind every assignee about their overdue
 * tasks, "sending a notification" is the kind of work that spends most of
 * its time WAITING - waiting on a network call to an email provider or an
 * SMS gateway, for example. If we sent ten reminders one after another on
 * a single thread, and each one took 200 milliseconds, the whole operation
 * would take roughly 2 seconds (10 x 200ms). Since each notification does
 * not depend on any other, we can send several of them AT THE SAME TIME
 * instead, on different threads, so the total time is much closer to the
 * time it takes to send just ONE notification.
 *
 * THREAD VS PROCESS (A COMMON INTERVIEW QUESTION)
 * -------------------------------------------------------------------------
 * A PROCESS is a running program with its own private memory space (for
 * example, our whole Spring Boot application is one process, or "java.exe"
 * as an OS-level process). A THREAD is a smaller unit of execution INSIDE
 * a process; every thread of the same process shares that process's heap
 * memory. This is exactly why the RequestMetrics counter (a shared object
 * on the heap) needs to be thread-safe: many threads inside our ONE
 * process can all reach the same object at once. Two different processes,
 * by contrast, cannot directly share plain Java objects in memory at all.
 *
 * RUNNABLE VS CALLABLE
 * -------------------------------------------------------------------------
 * Runnable is an older functional interface with a single "void run()"
 * method - it cannot return a result or throw a checked exception.
 * Callable<V> is the modern equivalent used with ExecutorService: its
 * single method is "V call() throws Exception", so it CAN return a result
 * (here, a NotificationResult) and CAN throw a checked exception. We use
 * Callable below because we genuinely want each simulated notification to
 * report back whether it succeeded.
 *
 * WHY ExecutorService INSTEAD OF CREATING new Thread(...) DIRECTLY?
 * -------------------------------------------------------------------------
 * Creating a brand new OS thread for every single task is expensive and,
 * with enough concurrent requests, can exhaust system resources.
 * ExecutorService manages a POOL of reusable worker threads for us: we
 * submit units of work (Callables), and the pool hands each one to a free
 * worker thread, queuing extra work if every thread is currently busy.
 * This is the same idea Tomcat itself uses to serve HTTP requests, and it
 * is the standard, production-grade way to run concurrent work in Java -
 * manually creating and managing raw Thread objects is rarely the right
 * choice in real backend code.
 */
@Service
public class NotificationService {

    private final TaskService taskService;

    // A fixed pool of 4 worker threads. Fixed-size pools are a simple,
    // predictable choice for this kind of short-lived, I/O-bound work -
    // large enough to get real parallelism, small enough not to overwhelm
    // whatever external system we are "notifying" (a real email provider
    // often has its own rate limits).
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public NotificationService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Finds every overdue task and, for each one that has an assignee,
     * simulates sending a reminder email CONCURRENTLY, then waits for
     * every send to finish before returning a combined list of results.
     */
    public List<NotificationResult> sendOverdueTaskReminders() {
        List<Task> overdueTasks = taskService.getOverdueTasks();

        // Step 1: submit() hands each Callable to the executor and
        // returns IMMEDIATELY with a Future<NotificationResult> - a
        // placeholder for a result that may not exist yet, because the
        // real work could still be running on a worker thread. Because we
        // submit every task in this loop before waiting for any of them,
        // the executor's worker threads can process several of them in
        // parallel.
        List<Future<NotificationResult>> pendingResults = new ArrayList<>();
        for (Task task : overdueTasks) {
            User assignee = task.getAssignee();
            if (assignee == null) {
                continue; // Nobody to notify for this task.
            }
            Callable<NotificationResult> sendReminderTask = () -> simulateSendingReminder(task, assignee);
            pendingResults.add(executorService.submit(sendReminderTask));
        }

        // Step 2: future.get() BLOCKS the calling thread until that
        // specific Future's task has actually finished, then returns its
        // result (or re-throws any exception the task threw, wrapped in
        // an ExecutionException). We collect results in submission order
        // here only for predictable output - the underlying sends may
        // have actually completed in a different order.
        List<NotificationResult> results = new ArrayList<>();
        for (Future<NotificationResult> pendingResult : pendingResults) {
            try {
                results.add(pendingResult.get(2, TimeUnit.SECONDS));
            } catch (InterruptedException interruptedException) {
                // InterruptedException is a CHECKED exception that means
                // "another thread asked this thread to stop waiting".
                // Best practice is to restore the interrupt flag so code
                // further up the call stack can also notice and react to
                // it, instead of silently swallowing the signal.
                Thread.currentThread().interrupt();
                results.add(new NotificationResult(null, null, false, "Interrupted while waiting for send"));
            } catch (ExecutionException | java.util.concurrent.TimeoutException executionProblem) {
                results.add(new NotificationResult(null, null, false, executionProblem.getMessage()));
            }
        }
        return results;
    }

    /**
     * Simulates the work of actually calling an email/SMS provider. We use
     * Thread.sleep(...) here purely to stand in for real network latency
     * in this learning project - a real implementation would call an
     * external API instead.
     */
    private NotificationResult simulateSendingReminder(Task task, User assignee) throws InterruptedException {
        Thread.sleep(200);
        return new NotificationResult(task.getId(), assignee.getEmail(), true,
                "Reminder sent for task '" + task.getTitle() + "'");
    }

    /**
     * BEAN LIFECYCLE: WHY DO WE NEED @PreDestroy HERE?
     * -------------------------------------------------------------------------
     * The ExecutorService above keeps its worker threads alive in the
     * background, waiting for work, until we explicitly tell it to stop.
     * If we never shut it down, those threads would keep the JVM process
     * alive even after Spring's application context has otherwise
     * finished shutting down. @PreDestroy tells Spring "call this method
     * automatically, exactly once, while this bean is being destroyed
     * during application shutdown" - the natural place to release a
     * resource a bean is responsible for, the same idea as closing a file
     * or a database connection.
     */
    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
    }
}
