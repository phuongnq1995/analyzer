package org.phuongnq.analyzer.service.recommendation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.repository.ShopRepository;
import org.phuongnq.analyzer.repository.entity.EvaluateEfficiency;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.service.event.IngestEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestEventHandler {

    private final EvaluateCampaignAIService evaluateCampaignAIService;
    private final ShopRepository shopRepository;
    private final RecommendationService recommendationService;

    @Async
    @EventListener
    @Transactional
    public void checkAndStartEvaluate(IngestEvent ingestEvent) {
        Shop shop = shopRepository.findById(ingestEvent.getSid()).orElseThrow();

        EvaluateEfficiency evaluateEfficiency = evaluateCampaignAIService.getEvaluateEfficiency(shop);

        recommendationService.saveEvaluateEfficiency(evaluateEfficiency);
    }

}
