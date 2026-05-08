package org.phuongnq.analyzer.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.aff.RecommendationDto;
import org.phuongnq.analyzer.service.recommendation.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService service;

    @GetMapping
    public ResponseEntity<RecommendationDto> get() {
        return ResponseEntity.ok(service.getRecommendation());
    }

}
