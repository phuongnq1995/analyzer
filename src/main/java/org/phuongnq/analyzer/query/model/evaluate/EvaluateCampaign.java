package org.phuongnq.analyzer.query.model.evaluate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateCampaign {
    private LocalDate date;
    private int clicks;
    private int orders;
    private BigDecimal spent;
    private float cpc;
    private float conversionRate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal revenue;
    private float roas;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal netProfit;

    @JsonIgnore
    private float orderPercentage;
    @JsonIgnore
    private float revenuePercentage;

    public EvaluateCampaign(LocalDate date, CampaignEfficiency efficiency, float orderPercentage, float revenuePercentage) {
        this.date = date;
        this.clicks = efficiency.getClicks();
        this.orders = efficiency.getOrders();
        this.spent = efficiency.getSpent();
        this.cpc = efficiency.getCpc();
        this.conversionRate = efficiency.getConversionRate() * 100;
        this.revenue = efficiency.getCommission();
        this.roas = efficiency.getRoas();
        this.netProfit = efficiency.getNetProfit();

        this.orderPercentage = orderPercentage;
        this.revenuePercentage = revenuePercentage;
    }
}
