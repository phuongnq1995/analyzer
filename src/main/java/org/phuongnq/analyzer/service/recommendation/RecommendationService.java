package org.phuongnq.analyzer.service.recommendation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.phuongnq.analyzer.dto.aff.EvaluateCampaignDto;
import org.phuongnq.analyzer.dto.aff.RecommendationDto;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.query.model.DelayPeriod;
import org.phuongnq.analyzer.query.model.evaluate.EfficiencyResults;
import org.phuongnq.analyzer.query.model.evaluate.EvaluateCampaign;
import org.phuongnq.analyzer.repository.ConversionCurvePercentageRepository;
import org.phuongnq.analyzer.repository.EvaluateEfficiencyRepository;
import org.phuongnq.analyzer.repository.UserImportRepository;
import org.phuongnq.analyzer.repository.entity.ConversionCurvePercentage;
import org.phuongnq.analyzer.repository.entity.EvaluateCampaignEfficiency;
import org.phuongnq.analyzer.repository.entity.EvaluateEfficiency;
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
    private final CacheService cacheService;
    private final UserImportRepository userImportRepository;
    private final ConversionCurvePercentageRepository repository;
    private final EvaluateCampaignAIService evaluateCampaignAIService;
    private final EvaluateEfficiencyRepository efficiencyRepository;

    @Transactional(readOnly = true)
    public RecommendationDto getRecommendation() {
        Shop shop = userService.getCurrentShop();

        Optional<EvaluateEfficiency> efficiencyOpt = efficiencyRepository.findTop1ByShopOrderByEvaluateDateDesc(shop);

        if (efficiencyOpt.isEmpty()) {
            return RecommendationDto.builder()
                .evaluateDate(LocalDate.now())
                .build();
        }

        EvaluateEfficiency latestEvaluate = efficiencyOpt.get();

        List<EvaluateCampaignDto> evaluateCampaigns = latestEvaluate.getCampaignEfficiencies()
            .stream()
            .map(EvaluateCampaignDto::new)
            .toList();

        return RecommendationDto.builder()
            .evaluateDate(latestEvaluate.getEvaluateDate())
            .evaluateCampaigns(evaluateCampaigns)
            .build();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndStartEvaluate(Shop shop) {

        // Business date
        LocalDate today = LocalDate.now();
        LocalDate businessDate = LocalDate.now().minusDays(1);

        boolean hasBothImportByDataDate = userImportRepository.hasBothImportByDataDate(shop.getId(), businessDate, today);

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
            .filter(filterValidCampaign(businessDate))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (validEvaluateCampaigns.isEmpty()) {
            log.warn("There are no valid evaluate campaigns for {}, {}", shop.getId(), businessDate);
            return;
        }

        EvaluateEfficiency evaluateEfficiency = EvaluateEfficiency.builder()
            .shop(shop)
            .evaluateDate(businessDate)
            .createdTime(Instant.now())
            .build();

        List<String> errorCampaigns = new ArrayList<>();

        for (Map.Entry<String, List<EvaluateCampaign>> entry : validEvaluateCampaigns.entrySet()) {

            try {

                EfficiencyResults results = evaluateCampaignAIService.evaluateCampaign(shop, entry, businessDate);

                EvaluateCampaignEfficiency evaluateCampaignEfficiency = EvaluateCampaignEfficiency.builder()
                    .shop(shop)
                    .evaluateEfficiency(evaluateEfficiency)
                    .name(entry.getKey())
                    .efficiencyLevel(results.getEfficiencyLevel())
                    .briefStatusSummary(results.getBriefStatusTags())
                    .recommendedActions(results.getRecommendedActions())
                    .build();

                evaluateEfficiency.addCampaignEfficiency(evaluateCampaignEfficiency);

            } catch (Exception e) {
                log.error("Error", e);
                errorCampaigns.add("campaign: %s, error: %s".formatted(entry.getKey(), e.getMessage()));

                // TODO: Adding to errorList and retry later
            }
        }

        String status = String.join(",", errorCampaigns);

        evaluateEfficiency.setErrorStatus(status);

        efficiencyRepository.save(evaluateEfficiency);
    }

    @NotNull
    private static Predicate<Entry<String, List<EvaluateCampaign>>> filterValidCampaign(LocalDate businessDate) {
        return stringListEntry -> stringListEntry.getValue().size() >= 5
            && stringListEntry.getValue().stream().map(EvaluateCampaign::getDate)
            .anyMatch(date -> date.isEqual(businessDate));
    }
}
