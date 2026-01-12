package org.phuongnq.analyzer.dto.aff;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        this.recommendedActions = StringUtils.split(campaignEfficiency.getRecommendedActions(), ".");
    }
}
