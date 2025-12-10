package org.phuongnq.analyzer.query.model;

import java.util.List;
import lombok.Data;

@Data
public class AggregationResult {
    public List<HourlyStat> hourlyStats;
    public List<ProductStat> productStats;
}
