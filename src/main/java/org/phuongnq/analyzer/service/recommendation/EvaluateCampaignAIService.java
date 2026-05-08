package org.phuongnq.analyzer.service.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.query.model.DelayPeriod;
import org.phuongnq.analyzer.query.model.evaluate.EfficiencyResults;
import org.phuongnq.analyzer.query.model.evaluate.EvaluateCampaign;
import org.phuongnq.analyzer.query.model.evaluate.IncompletedCampaignResults;
import org.phuongnq.analyzer.repository.ConversionCurvePercentageRepository;
import org.phuongnq.analyzer.repository.UserImportRepository;
import org.phuongnq.analyzer.repository.entity.ConversionCurvePercentage;
import org.phuongnq.analyzer.repository.entity.EvaluateCampaignEfficiency;
import org.phuongnq.analyzer.repository.entity.EvaluateEfficiency;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.service.CacheService;
import org.phuongnq.analyzer.utils.MathUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluateCampaignAIService {

    private final ObjectMapper objectMapper;
    private final AICallService aiCallService;
    private final CacheService cacheService;
    private final UserImportRepository userImportRepository;
    private final ConversionCurvePercentageRepository repository;

    @Value("classpath:prompts/alert-system.md")
    private Resource alertSystemResource;

    @Transactional(readOnly = true)
    public EvaluateEfficiency getEvaluateEfficiency(Shop shop) {
        // Business date
        LocalDate today = LocalDate.now();
        LocalDate businessDate = LocalDate.now().minusDays(1);

        boolean hasBothImportByDataDate = userImportRepository.hasBothImportByDataDate(shop.getId(), businessDate, today);

        if (!hasBothImportByDataDate) {
            return null;
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
            .filter(entry -> filterValidCampaign(entry.getValue(), businessDate))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (validEvaluateCampaigns.isEmpty()) {
            log.warn("There are no valid evaluate campaigns for {}, {}", shop.getId(), businessDate);
            return null;
        }

        EvaluateEfficiency evaluateEfficiency = EvaluateEfficiency.builder()
            .shop(shop)
            .evaluateDate(businessDate)
            .createdTime(Instant.now())
            .build();

        List<String> errorCampaigns = new ArrayList<>();
        BigDecimal salesTax = shop.getSalesTax();
        BigDecimal marketingFee = shop.getMarketingFee();

        for (Map.Entry<String, List<EvaluateCampaign>> entry : validEvaluateCampaigns.entrySet()) {

            try {

                log.info("Evaluate performance for campaign: {}", entry.getKey());

                EfficiencyResults results = evaluateCampaign(salesTax, marketingFee, entry.getValue());

                String recommendedActions = String.join(EvaluateCampaignEfficiency.DELIMITER,
                    results.getRecommendedActions());

                EvaluateCampaignEfficiency evaluateCampaignEfficiency = EvaluateCampaignEfficiency.builder()
                    .shop(shop)
                    .evaluateEfficiency(evaluateEfficiency)
                    .name(entry.getKey())
                    .efficiencyLevel(results.getEfficiencyLevel())
                    .briefStatusSummary(results.getBriefStatusSummary())
                    .recommendedActions(recommendedActions)
                    .build();

                evaluateEfficiency.addCampaignEfficiency(evaluateCampaignEfficiency);

            } catch (Exception e) {
                log.error("Error", e);
                errorCampaigns.add("campaign: %s, error: %s".formatted(entry.getKey(), e.getClass().getSimpleName()));

                // TODO: Adding to errorList and retry later
            }
        }

        String status = String.join(",", errorCampaigns);

        evaluateEfficiency.setErrorStatus(status);

        return evaluateEfficiency;
    }

    private EfficiencyResults evaluateCampaign(BigDecimal salesTax, BigDecimal marketingFee, List<EvaluateCampaign> campaigns)
        throws IOException {

        campaigns.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        Map<Boolean, List<EvaluateCampaign>> listMap = campaigns.stream()
            .collect(Collectors.groupingBy(EvaluateCampaignAIService::isIncomplete));

        String incompleteDataStr = listMap.getOrDefault(true, List.of()).stream()
            .map(campaign -> new IncompletedCampaignResults(campaign, salesTax, marketingFee))
            .map(this::objectToString)
            .collect(Collectors.joining("\n"));

        String completeDataStr = listMap.getOrDefault(false, List.of()).stream()
            .map(this::objectToString)
            .collect(Collectors.joining("\n"));

        String userPrompt = """
            Evaluate this campaign with provided data:
            
            # Last incomplete days data
            
            %s
            # History data
            
            %s
            """.formatted(incompleteDataStr, completeDataStr);

        log.info("Input data: {}", userPrompt);

        EfficiencyResults results = aiCallService.callAI(userPrompt,
            alertSystemResource.getContentAsString(Charset.defaultCharset()));

        log.info("Results: {}", results);

        return results;
    }

    private static boolean filterValidCampaign(List<EvaluateCampaign> values, LocalDate businessDate) {
        if (values.size() < 3) {
            return false;
        }
        return values.stream()
            .map(EvaluateCampaign::getDate)
            .anyMatch(date -> date.isEqual(businessDate));
    }

    private String objectToString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error: {}", e);
            throw new RuntimeException(e);
        }
    }

    public static boolean isIncomplete(EvaluateCampaign campaign) {
        return campaign.getOrderPercentage() < 100 && campaign.getRevenuePercentage() < 100;
    }
}
