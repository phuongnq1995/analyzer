package org.phuongnq.analyzer.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.service.RecommendationService;
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
    public ResponseEntity<List> get() {
        return ResponseEntity.ok(service.getRecommendations());
    }

}
