package org.phuongnq.analyzer.dto.aff;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecommendationDto {
    private LocalDate evaluateDate;
    private List<EvaluateCampaignDto> evaluateCampaigns;
}
