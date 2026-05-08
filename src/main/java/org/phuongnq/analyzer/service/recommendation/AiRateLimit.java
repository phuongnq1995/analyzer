package org.phuongnq.analyzer.service.recommendation;

import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiRateLimit {
    private String model;
    private int priority;
    private Bucket requestPerMinute;
    private Bucket requestPerDay;
}
