package org.phuongnq.analyzer.query.model;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregationByDateResult {
    private LocalDate date;
    private List<CampaignEfficiency> campaignEfficiencies;
}
