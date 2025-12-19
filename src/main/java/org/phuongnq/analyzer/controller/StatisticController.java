package org.phuongnq.analyzer.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.service.AggregationStatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticController {

    private final AggregationStatisticService service;

    @GetMapping
    public ResponseEntity<List<AggregationByDateResult>> get(
        @RequestParam(value = "from", required = false) LocalDate from,
        @RequestParam(value = "to", required = false) LocalDate to,
        @RequestParam(value = "type", defaultValue = "clickTime") String type) {

        if (to == null) {
            to = LocalDate.now();
        }

        return ResponseEntity.ok(service.getCompareAggregationStatistics(from, to, type));
    }
}
