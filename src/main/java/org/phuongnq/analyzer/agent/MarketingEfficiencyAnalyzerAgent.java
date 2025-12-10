package org.phuongnq.analyzer.agent;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.WaitFor;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.prompt.persona.RoleGoalBackstory;
import com.embabel.common.ai.model.LlmOptions;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.query.AggregationQuery;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.core.io.Resource;

@Agent(
    name = "MarketingEfficiencyAnalyzer",
    description = "Analyze marketing efficiency based on recent results and suggest improvements."
)
@Slf4j
@RequiredArgsConstructor
public class MarketingEfficiencyAnalyzerAgent {

    @Value("classpath:prompts/marketing-analyzer.txt")
    private Resource promptTemplate;

    private final AggregationQuery aggregationQuery;

    @Action
    public CampaignRetrievalInput extractRange(UserInput userInput, OperationContext context) {
        String prompt = """
                Today is %s.
                Based on this date, extract the campaignId, fromDate and toDate from user input.
                Provide the result in the format: CampaignRetrievalInput(campaignId, fromDate=YYYY-MM-DD, toDate=YYYY-MM-DD).

                User input:
                %s""".formatted(LocalDate.now().toString(), userInput.getContent());

        log.info("Prompt for date range extraction: {}", prompt);

        return context.ai()
            .withDefaultLlm()
            .createObject(prompt, CampaignRetrievalInput.class);
    }

    @Action(cost = 100.0)
    CampaignRetrievalInput askForCampaignRetrievalInput(OperationContext context) {
        return WaitFor.formSubmission("Please provide the campaignId, fromDate, toDate for retrieveCampaignData.", CampaignRetrievalInput.class);
    }

    @Action
    public CampaignDataListByDate retrieveListCampaignData(CampaignRetrievalInput input) {
        return new CampaignDataListByDate(aggregationQuery.getMarketingEfficiency(input.campaignId(), input.fromDate(), input.toDate()));
    }

    @Action
    @AchievesGoal(description = "Analyze marketing efficiency based on data provided and suggest improvements.")
    public CampaignEfficiencyResult analyzeMarketingEfficiency(CampaignDataListByDate campaignDataListByDate, OperationContext context)
        throws IOException {

        String prompt = promptTemplate.getContentAsString(Charset.defaultCharset()).toString();

        log.info("Prompt for analysis: {}", prompt);

        return context.ai()
            .withDefaultLlm()
            .withToolObject(aggregationQuery)
            .createObject(prompt, CampaignEfficiencyResult.class);
    }
}

record CampaignRetrievalInput(String campaignId, LocalDate fromDate, LocalDate toDate) {
}

@Data
class CampaignDataListByDate {

    private List<CampaignDataByDate> dataByDates;

    public CampaignDataListByDate(List<CampaignEfficiency> campaignEfficiencies) {
        this.dataByDates = campaignEfficiencies.stream()
            .map(e -> new CampaignDataByDate(e))
            .collect(Collectors.toList());
    }
}

@Data
class CampaignDataByDate {
    @JsonPropertyDescription("Date")
    public LocalDate date;
    @JsonPropertyDescription("Ad click")
    public int adClicks;
    @JsonPropertyDescription("Number of product ordered")
    public int numberOfOrders;
    @JsonPropertyDescription("Ad spent amount")
    public BigDecimal adSpent;
    @JsonPropertyDescription("Revenue")
    public BigDecimal revenue;
    @JsonPropertyDescription("Cost per click")
    public float cpc;
    @JsonPropertyDescription("ConversionRate")
    public BigDecimal conversionRate;
    @JsonPropertyDescription("Profit amount")
    public BigDecimal profit;

    public CampaignDataByDate(CampaignEfficiency e) {
        this.date = e.getDate();
        this.adClicks = e.getClicks();
        this.numberOfOrders = e.getOrders();
        this.adSpent = e.getSpent();
        this.revenue = e.getRevenue();
        this.cpc = e.getCpc();
        this.conversionRate = e.getConversionRate();
        this.profit = e.getRevenue();
    }
}