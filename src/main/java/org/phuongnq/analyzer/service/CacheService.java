package org.phuongnq.analyzer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {

    private static final String CLICK_KEY = "{sid:%d}:aggregate-click";
    private static final String ORDER_KEY = "{sid:%d}:aggregate-order";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper mapper;

    public void cacheAggregateByClick(Long sId, LocalDate start, LocalDate end, List<AggregationByDateResult> data) {
        cacheAggregates(getKey(sId, true), start, end, data);
    }

    public void cacheAggregateByOrder(Long sId, LocalDate start, LocalDate end, List<AggregationByDateResult> data) {
        cacheAggregates(getKey(sId, false), start, end, data);
    }

    public List<AggregationByDateResult> getAggregateByClick(Long sId, LocalDate start, LocalDate end) {
        return getAggregates(getKey(sId, true), start, end);
    }

    public List<AggregationByDateResult> getAggregateByOrder(Long sId, LocalDate start, LocalDate end) {
        return getAggregates(getKey(sId, false), start, end);
    }

    private void cacheAggregates(String key, LocalDate start, LocalDate end, List<AggregationByDateResult> data) {
        // Remove old rank
        redisTemplate.opsForZSet().removeRangeByScore(key, start.toEpochDay(), end.toEpochDay());

        Set<TypedTuple<Object>> tuples = new HashSet<>();

        for (AggregationByDateResult result : data) {
            tuples.add(new DefaultTypedTuple<>(result, (double) result.getDate().toEpochDay()));
        }

        redisTemplate.opsForZSet().add(key, tuples);
    }

    private List<AggregationByDateResult> getAggregates(String key, LocalDate start, LocalDate end) {
        return redisTemplate.opsForZSet().rangeByScore(key, start.toEpochDay(), end.toEpochDay())
            .stream()
            .map(data -> mapper.convertValue(data, AggregationByDateResult.class))
            .toList();
    }

    private static String getKey(Long sId, boolean isClick) {
        return isClick ? CLICK_KEY.formatted(sId) : ORDER_KEY.formatted(sId);
    }
}
