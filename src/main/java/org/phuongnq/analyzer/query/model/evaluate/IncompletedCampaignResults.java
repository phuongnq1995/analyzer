package org.phuongnq.analyzer.query.model.evaluate;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncompletedCampaignResults {
    private LocalDate date;
    private int clicks;
    private BigDecimal spent;
    private float cpc;
    private int currentOrders;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal currentRevenue;
    private int estimateOrders;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal estimateRevenue;

    public IncompletedCampaignResults(EvaluateCampaign efficiency) {
        this.date = efficiency.getDate();
        this.clicks = efficiency.getClicks();
        this.currentOrders = efficiency.getOrders();
        this.spent = efficiency.getSpent();
        this.cpc = efficiency.getCpc();
        this.currentRevenue = efficiency.getRevenue();

        this.estimateOrders = BigDecimal.valueOf(currentOrders).divide(BigDecimal.valueOf(efficiency.getOrderPercentage() / 100), 2, RoundingMode.HALF_UP).intValue();
        this.estimateRevenue = currentRevenue.divide(BigDecimal.valueOf(efficiency.getRevenuePercentage() / 100), 2, RoundingMode.HALF_UP);
    }
}
