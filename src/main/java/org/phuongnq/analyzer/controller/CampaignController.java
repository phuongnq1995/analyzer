package org.phuongnq.analyzer.controller;

import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.info.ShopSettings;
import org.phuongnq.analyzer.service.MappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final MappingService service;

    @GetMapping
    public ResponseEntity<Void> get() {
        service.getCampaigns();
        return ResponseEntity.ok().build();
    }

}
