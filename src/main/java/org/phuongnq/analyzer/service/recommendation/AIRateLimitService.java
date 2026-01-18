package org.phuongnq.analyzer.service.recommendation;

import io.github.bucket4j.Bucket;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collection;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIRateLimitService {

    private final Collection<AiRateLimit> modelLimits = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        resetModelLimits();
    }

    @Scheduled(cron = "@daily", zone = "America/Los_Angeles")
    public void resetRateLimit() {
        log.info("Resetting AI model rate limits");
        resetModelLimits();
    }

    public String getAvailableModel() {
        return modelLimits.stream()
            .sorted(Comparator.comparingInt(AiRateLimit::getPriority))
            .filter(this::filterAiRateLimit)
            .findFirst()
            .map(AiRateLimit::getModel)
            .orElseThrow(() -> new RuntimeException("AI rate limit exceeded for all models"));
    }

    public void lockModel(String model) {
        log.warn("Locking AI model {} due to error.", model);
        modelLimits.stream()
            .filter(rateLimit -> rateLimit.getModel().equals(model))
            .findFirst()
            .ifPresent(rateLimit -> {
                // Consume a large number of tokens to effectively lock the model
                rateLimit.getRequestPerMinute().tryConsumeAsMuchAsPossible();
                rateLimit.getRequestPerDay().tryConsumeAsMuchAsPossible();
            });
    }

    private boolean filterAiRateLimit(AiRateLimit rateLimit) {
        return rateLimit.getRequestPerDay().tryConsume(1) && rateLimit.getRequestPerMinute().tryConsume(1);
    }

    private static AiRateLimit createRateLimit(String name, int priority, int rpm, int rpd) {
        return new AiRateLimit(
            name,
            priority,
            createBucket(rpm, Duration.ofMinutes(1)),
            createBucket(rpd, Duration.ofDays(1))
        );
    }

    private static Bucket createBucket(int capacity, Duration refillDuration) {
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, refillDuration))
            .build();
    }

    private void resetModelLimits() {
        modelLimits.clear();

        modelLimits.add(createRateLimit("gemini-2.5-flash", 0, 5, 20));
        modelLimits.add(createRateLimit("gemini-2.5-flash-lite", 10,10, 20));
        modelLimits.add(createRateLimit("gemini-3-flash-preview", 20, 5, 20));
        modelLimits.add(createRateLimit("gemma-3-27b-it",30, 30, 20));
        modelLimits.add(createRateLimit("gemma-3-12b-it", 40, 30, 14400));
        modelLimits.add(createRateLimit("gemma-3-4b-it", 50, 30, 14400));
        modelLimits.add(createRateLimit("gemma-3-1b-it", 70, 30, 14400));
    }

}
