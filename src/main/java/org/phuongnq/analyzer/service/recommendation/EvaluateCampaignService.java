package org.phuongnq.analyzer.service.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.query.model.evaluate.EfficiencyResults;
import org.phuongnq.analyzer.query.model.evaluate.EvaluateCampaign;
import org.phuongnq.analyzer.query.model.evaluate.IncompletedCampaignResults;
import org.phuongnq.analyzer.repository.EvaluateCampaignEfficiencyRepository;
import org.phuongnq.analyzer.repository.entity.EvaluateCampaignEfficiency;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluateCampaignService {

    private final EvaluateCampaignEfficiencyRepository repository;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    @Value("classpath:prompts/alert-system.md")
    private Resource alertSystemResource;

    public void evaluateCampaign(Shop shop, Entry<String, List<EvaluateCampaign>> entry, LocalDate businessDate) {

        try {

            List<EvaluateCampaign> campaigns = entry.getValue();

            campaigns.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

            Map<Boolean, List<EvaluateCampaign>> listMap = campaigns.stream()
                .collect(Collectors.groupingBy(evaluateCampaign -> isIncomplete(evaluateCampaign)));

            List<EvaluateCampaign> incompleteData = listMap.get(true);

            String incompleteDataStr = incompleteData.stream()
                .map(IncompletedCampaignResults::new)
                .map(this::objectToString)
                .collect(Collectors.joining("\n"));

            String completeDataStr = listMap.get(false).stream()
                .map(this::objectToString)
                .collect(Collectors.joining("\n"));

            String userPrompt = """
                Evaluate this campaign with provided data:
                
                # Last 3 days data
                
                %s
                # History data
                
                %s
                """.formatted(incompleteDataStr, completeDataStr);

            EfficiencyResults results = chatClient.prompt().user(userPrompt)
                .system(alertSystemResource.getContentAsString(Charset.defaultCharset()))
                .advisors(new SimpleLoggerAdvisor()).call().entity(EfficiencyResults.class);

            log.info("Results: {}", results);

            EvaluateCampaignEfficiency evaluateCampaignEfficiency = EvaluateCampaignEfficiency.builder()
                .shop(shop)
                .name(entry.getKey())
                .efficiencyLevel(results.getEfficiencyLevel())
                .briefStatusSummary(results.getBriefStatusSummary())
                .recommendedActions(results.getRecommendedActions())
                .evaluateDate(businessDate)
                .createdTime(Instant.now())
                .build();

            repository.save(evaluateCampaignEfficiency);
        } catch (Exception e) {
            log.error("Error: {}", e);
        }
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
