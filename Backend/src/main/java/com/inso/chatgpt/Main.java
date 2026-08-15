package com.inso.chatgpt;

import java.time.Instant;
import java.util.List;

public class Main {

    private static DSA dsa = new DSA();
    public static void main(String[] args) {
        dsa.countIncidentsByCountry(incidents);
    }

    private static List<Incident> incidents = List.of(
            new Incident(
                    "INC-001",
                    "Germany",
                    "Network",
                    Instant.parse("2024-05-10T14:32:00Z"),
                    Severity.HIGH
            ),
            new Incident(
                    "INC-001",
                    "Germany",
                    "Network",
                    Instant.parse("2024-05-10T14:32:00Z"),
                    Severity.HIGH
            ),
            new Incident(
                    "INC-002",
                    "Netherlands",
                    "Database",
                    Instant.parse("2024-05-11T09:15:00Z"),
                    Severity.CRITICAL
            ),
            new Incident(
                    "INC-003",
                    "Spain",
                    "Authentication",
                    Instant.parse("2024-05-12T18:47:00Z"),
                    Severity.MEDIUM
            ),
            new Incident(
                    "INC-004",
                    "France",
                    "API",
                    Instant.parse("2024-05-13T07:22:00Z"),
                    Severity.LOW
            ),
            new Incident(
                    "INC-005",
                    "Italy",
                    "Infrastructure",
                    Instant.parse("2024-05-14T11:05:00Z"),
                    Severity.HIGH
            )
    );

}
