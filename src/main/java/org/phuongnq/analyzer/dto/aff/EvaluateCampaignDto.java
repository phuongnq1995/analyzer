package org.phuongnq.analyzer.dto.aff;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.phuongnq.analyzer.query.model.EfficiencyLevel;
import org.phuongnq.analyzer.repository.entity.EvaluateCampaignEfficiency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateCampaignDto {
    private String name;
    private EfficiencyLevel level;
    private String briefSummary;
    private String[] recommendedActions;

    public EvaluateCampaignDto(EvaluateCampaignEfficiency campaignEfficiency) {
        this.name = campaignEfficiency.getName();
        this.level = campaignEfficiency.getEfficiencyLevel();
        this.briefSummary = campaignEfficiency.getBriefStatusSummary();
        this.recommendedActions = campaignEfficiency.getRecommendedActions().split(EvaluateCampaignEfficiency.DELIMITER_ENCODE);
    }
}
