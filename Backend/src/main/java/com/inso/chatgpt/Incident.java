package com.inso.chatgpt;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
@Data
@AllArgsConstructor
public class Incident {
    private String id;
    private String country;
    private String category;
    private Instant timestamp;
    private Severity severity;
}
