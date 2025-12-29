package org.phuongnq.analyzer.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.info.RegisterRequest;
import org.phuongnq.analyzer.dto.info.UserInfo;
import org.phuongnq.analyzer.dto.shop.CampaignDto;
import org.phuongnq.analyzer.dto.shop.MappingDto;
import org.phuongnq.analyzer.dto.shop.OrderLinkDto;
import org.phuongnq.analyzer.repository.entity.User;
import org.phuongnq.analyzer.service.MappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mapping")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService service;

    @GetMapping("/campaigns")
    public ResponseEntity<List<CampaignDto>> getCampaigns() {
        return ResponseEntity.ok(service.getCampaigns());
    }

    @GetMapping("/orderLinks")
    public ResponseEntity<List<OrderLinkDto>> getLinks() {
        return ResponseEntity.ok(service.getOrderLinks());
    }

    @PostMapping("/campaigns/{campaignId}/orderLinks/{linkId}")
    public ResponseEntity<Void> register(@PathVariable("campaignId") Long campaignId,
            @PathVariable("linkId") Long linkId) {
        service.updateMapping(campaignId, linkId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/campaigns/{campaignId}")
    public ResponseEntity<Void> removeMapping(@PathVariable("campaignId") Long campaignId) {
        service.deleteCampaignMapping(campaignId);
        return ResponseEntity.ok().build();
    }
}
