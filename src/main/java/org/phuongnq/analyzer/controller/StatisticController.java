package org.phuongnq.analyzer.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.AggregationResult;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.service.AggregationStatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatisticController {

    private final AggregationStatisticService service;

    @GetMapping
    public ResponseEntity<List<CampaignEfficiency>> get(
        @RequestParam(value = "from") LocalDate from, @RequestParam(value = "to") LocalDate to) {
        return ResponseEntity.ok(service.getMarketingEfficiency(from, to));
    }

    @GetMapping("/compare")
    public ResponseEntity<List<AggregationByDateResult>> compare(
        @RequestParam(value = "from") LocalDate from, @RequestParam(value = "to") LocalDate to) {
        return ResponseEntity.ok(service.getCompareAggregationStatistics(from, to));
    }
}
