package com.inso.learning.taskflow.domain;

/**
 * Represents how urgent a Task is. We use this later with Comparable and
 * Comparator to sort tasks, and with the Stream API to filter tasks by
 * urgency in the service layer.
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}
