package com.inso.learning.taskflow.domain;

/**
 * Represents how far along a Task is. A Task can only ever be in one of
 * these three states - using an enum lets the compiler (and our IDE) check
 * a "switch" over a TaskStatus for completeness.
 */
public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE
}
