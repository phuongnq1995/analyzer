package org.phuongnq.analyzer.service.recommendation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.query.model.evaluate.EfficiencyResults;
import org.phuongnq.analyzer.query.model.evaluate.EvaluateCampaign;
import org.phuongnq.analyzer.query.model.evaluate.IncompletedCampaignResults;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluateCampaignAIService {

    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    @Value("classpath:prompts/alert-system.md")
    private Resource alertSystemResource;

    public EfficiencyResults evaluateCampaign(Shop shop, List<EvaluateCampaign> campaigns)
        throws IOException {

        campaigns.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        Map<Boolean, List<EvaluateCampaign>> listMap = campaigns.stream()
            .collect(Collectors.groupingBy(evaluateCampaign -> isIncomplete(evaluateCampaign)));

        List<EvaluateCampaign> incompleteData = listMap.get(true);

        String incompleteDataStr = incompleteData.stream()
            .map(campaign -> new IncompletedCampaignResults(campaign, shop))
            .map(this::objectToString)
            .collect(Collectors.joining("\n"));

        String completeDataStr = listMap.get(false).stream()
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

        EfficiencyResults results = chatClient.prompt().user(userPrompt)
            .system(alertSystemResource.getContentAsString(Charset.defaultCharset()))
            .advisors(new SimpleLoggerAdvisor()).call().entity(EfficiencyResults.class);

        log.info("Results: {}", results);

        return results;
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
