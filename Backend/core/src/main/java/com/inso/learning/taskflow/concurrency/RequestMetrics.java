package com.inso.learning.taskflow.concurrency;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * =============================================================================
 * STAGE 10: CONCURRENCY - A THREAD-SAFE REQUEST COUNTER
 * =============================================================================
 *
 * WHY DOES THIS CLASS EXIST?
 * -------------------------------------------------------------------------
 * Every incoming HTTP request in a Spring Boot application is handled on
 * its OWN thread, borrowed from a pool that the embedded Tomcat server
 * manages (this is a "Thread" - an independent path of execution that the
 * JVM can run in parallel with other threads on a multi-core CPU). If two
 * users call our API at almost the exact same moment, Tomcat may genuinely
 * run their two requests AT THE SAME TIME, on two different threads. This
 * class counts how many times each API endpoint has been called, and it
 * must do that safely even when many threads update the same counter at
 * once - see RequestMetricsFilter for where increment(...) is actually
 * called from.
 *
 * WHAT IS A RACE CONDITION, AND WHY WOULD A SIMPLE HashMap<String, Long>
 * FAIL HERE?
 * -------------------------------------------------------------------------
 * Imagine we stored counts in a plain HashMap<String, Long> and updated a
 * count with "counts.put(key, counts.get(key) + 1)". That single line is
 * actually THREE separate steps: read the current value, add one to it,
 * and write the new value back. If two threads both read the same
 * starting value (say 5) before either one writes back, they will both
 * compute 6 and store 6 - one increment is silently LOST, and the true
 * count should have been 7. This is called a RACE CONDITION: the final,
 * correct result depends on the unpredictable timing ("race") of multiple
 * threads, instead of being guaranteed by the code itself. Race conditions
 * are especially dangerous because they often do not show up in testing -
 * they may only appear once in a while, under real production load, which
 * makes them hard to reproduce and debug.
 *
 * HOW DO WE FIX THIS? OPTION 1: THE "synchronized" KEYWORD
 * -------------------------------------------------------------------------
 * One classic fix is to mark the whole read-increment-write operation as
 * "synchronized", which means only ONE thread at a time is allowed to
 * execute that block of code - every other thread trying to enter must
 * wait its turn:
 *
 *     private final Map<String, Long> counts = new HashMap<>();
 *     public synchronized void increment(String key) {
 *         counts.put(key, counts.getOrDefault(key, 0L) + 1);
 *     }
 *
 * This works and is simple to reason about, but it means every single
 * request - even for completely different endpoints - has to wait in a
 * single-file line to update the counter, which can become a bottleneck
 * under heavy traffic.
 *
 * HOW DO WE FIX THIS? OPTION 2 (WHAT WE ACTUALLY USE): ConcurrentHashMap
 * PLUS AtomicLong
 * -------------------------------------------------------------------------
 * ConcurrentHashMap is a version of HashMap built specifically to be safe
 * to read and write from many threads at once, WITHOUT needing us to add
 * our own "synchronized" blocks - internally, it only locks small
 * portions of the map at a time, so unrelated keys can still be updated
 * concurrently. AtomicLong is a number wrapper whose increment operation
 * (incrementAndGet()) is guaranteed by the JVM to be a single, indivisible
 * ("atomic") CPU-level operation - there is no "read, then write" gap for
 * another thread to interfere with. Combining the two gives us a
 * thread-safe counter map that also scales well, which is why this is the
 * standard, idiomatic way to solve this exact problem in real Java
 * backends - the "synchronized" version above is shown only so the
 * trade-off is clear.
 */
@Component
public class RequestMetrics {

    private final ConcurrentHashMap<String, AtomicLong> requestCountsByEndpoint = new ConcurrentHashMap<>();

    /**
     * "volatile" ON THIS FIELD - WHAT DOES IT ACTUALLY DO?
     * ---------------------------------------------------------------
     * Without "volatile", each CPU core is allowed to keep its own
     * cached copy of a field's value for speed. That means one thread
     * could flip "trackingEnabled" to false, while a different thread
     * running on a different core keeps reading its own stale cached
     * "true" value for an unpredictable amount of time. Marking a field
     * "volatile" tells the JVM: always read this field's latest value
     * directly, and always write changes immediately, so every thread
     * sees updates to it right away. This solves VISIBILITY between
     * threads - a different problem from the race condition above, which
     * was about multiple threads updating the SAME value unsafely.
     * "volatile" alone would NOT have fixed the counter increment race
     * condition, because "volatile" only guarantees visibility, not that
     * a multi-step operation like "read, add one, write" happens
     * atomically.
     */
    private volatile boolean trackingEnabled = true;

    public void increment(String endpoint) {
        if (!trackingEnabled) {
            return;
        }
        // computeIfAbsent(...) atomically creates a new AtomicLong(0) the
        // FIRST time a given endpoint is seen, and simply reuses it on
        // every later call - ConcurrentHashMap guarantees only one thread
        // ever creates the initial AtomicLong for a given key, even if
        // many threads call this method for a brand new endpoint at the
        // exact same moment.
        requestCountsByEndpoint.computeIfAbsent(endpoint, key -> new AtomicLong()).incrementAndGet();
    }

    /**
     * Returns a simple, read-only snapshot of the current counts. We
     * deliberately copy the values into a plain Map<String, Long> here
     * instead of exposing the internal ConcurrentHashMap<String,
     * AtomicLong> directly - callers (like MetricsController) should not
     * be able to reach in and mutate our internal AtomicLong instances.
     */
    public Map<String, Long> snapshot() {
        return requestCountsByEndpoint.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    public void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
    }
}
