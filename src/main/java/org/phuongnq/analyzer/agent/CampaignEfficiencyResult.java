package org.phuongnq.analyzer.agent;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record CampaignEfficiencyResult(
    @JsonPropertyDescription("Efficiency level evaluated")
    EfficiencyLevel results,
    @JsonPropertyDescription("Explanation for evaluation")
    String explanation,
    String advice
) {
    enum EfficiencyLevel {
        VERY_EFFICIENT, EFFICIENT, OK, BAD, VERY_BAD
    }
}
