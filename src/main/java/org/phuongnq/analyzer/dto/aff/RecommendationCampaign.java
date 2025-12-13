package org.phuongnq.analyzer.dto.aff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationCampaign {
    private Long id;
    private String campaignName;
    private int efficiencyLevel;
    private String action;
    private String advise;
}
