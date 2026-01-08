package org.phuongnq.analyzer.service.recommendation;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.phuongnq.analyzer.dto.aff.RecommendationCampaign;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.query.model.DelayPeriod;
import org.phuongnq.analyzer.query.model.evaluate.EvaluateCampaign;
import org.phuongnq.analyzer.repository.ConversionCurvePercentageRepository;
import org.phuongnq.analyzer.repository.UserImportRepository;
import org.phuongnq.analyzer.repository.entity.ConversionCurvePercentage;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.service.CacheService;
import org.phuongnq.analyzer.service.UserService;
import org.phuongnq.analyzer.utils.MathUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserService userService;
    private final AffQuery query;
    private final CacheService cacheService;
    private final UserImportRepository userImportRepository;
    private final ConversionCurvePercentageRepository repository;
    private final EvaluateCampaignService evaluateCampaignService;

    @Transactional(readOnly = true)
    public List<RecommendationCampaign> getRecommendations() {
        Long sid = userService.getCurrentShopId();
        Optional<Long> id = query.getLatestRecommendation(sid);
        if (id.isEmpty()) {
            return Collections.emptyList();
        }
        return query.getRecommendationCampaigns(id.get());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndStartEvaluate(Shop shop) {

        // Business date
        LocalDate businessDate = LocalDate.now().minusDays(1);

        boolean hasBothImportByDataDate = userImportRepository.hasBothImportByDataDate(shop.getId(), businessDate);

        if (!hasBothImportByDataDate) {
            return;
        }

        Map<Pair<String, DelayPeriod>, ConversionCurvePercentage> percentageMap = repository.findAll().stream()
            .collect(Collectors.toMap(ccp -> Pair.of(ccp.getName(), ccp.getPeriod()), Function.identity()));

        List<AggregationByDateResult> data = cacheService.getAggregateByClick(shop.getId(), businessDate.minusDays(6),
            businessDate);

        Map<String, List<EvaluateCampaign>> campaignEvaluates = new HashMap<>();

        for (AggregationByDateResult result : data) {

            List<CampaignEfficiency> campaignEfficiencies = result.getCampaignEfficiencies().stream().filter(
                campaignEfficiency -> campaignEfficiency.getClicks() != 0 && !MathUtils.isZero(
                    campaignEfficiency.getSpent())).toList();

            LocalDate date = result.getDate();
            int days = Period.between(date, businessDate).getDays();
            DelayPeriod delayPeriod = DelayPeriod.fromDelay(days);

            for (CampaignEfficiency efficiency : campaignEfficiencies) {

                String name = efficiency.getName();

                List<EvaluateCampaign> campaignList = campaignEvaluates.computeIfAbsent(name, s -> new ArrayList<>());

                ConversionCurvePercentage ccp = percentageMap.getOrDefault(Pair.of(name, delayPeriod),
                    ConversionCurvePercentage.defaultValue());

                campaignList.add(
                    new EvaluateCampaign(date, efficiency, ccp.getOrderPercentage(), ccp.getRevenuePercentage()));
            }
        }

        Map<String, List<EvaluateCampaign>> validEvaluateCampaigns = campaignEvaluates.entrySet().stream()
            .filter(stringListEntry -> stringListEntry.getValue().size() >= 5)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        for (Map.Entry<String, List<EvaluateCampaign>> entry : validEvaluateCampaigns.entrySet()) {
            evaluateCampaignService.evaluateCampaign(shop, entry, businessDate);
        }
    }

}
