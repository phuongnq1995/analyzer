package org.phuongnq.analyzer.query.model;

import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class AggregationByDateResult {
    private LocalDate date;
    private List<CampaignEfficiency> campaignEfficiencies;
}
