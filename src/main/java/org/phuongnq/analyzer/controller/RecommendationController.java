package org.phuongnq.analyzer.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.service.AggregationStatisticService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final AggregationStatisticService service;

    @GetMapping
    public ResponseEntity<List> get() {
        return ResponseEntity.ok(Collections.emptyList());
    }

}
