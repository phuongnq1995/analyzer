package org.phuongnq.analyzer.query.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignDateEfficiency {
    public LocalDate date;
    @JsonPropertyDescription("Ad click")
    public int adClicks;
    @JsonPropertyDescription("Number of product ordered")
    public int orders;
    @JsonPropertyDescription("Ad spent amount")
    public BigDecimal adSpent;
    @JsonPropertyDescription("Revenue")
    public BigDecimal revenue;
    @JsonPropertyDescription("Cost per click")
    public float cpc;
    @JsonPropertyDescription("ConversionRate")
    public BigDecimal conversionRate;
    @JsonPropertyDescription("Profit amount")
    public BigDecimal profit;
    @JsonPropertyDescription("Return as Ad spend")
    public BigDecimal roas;

    public CampaignDateEfficiency(LocalDate date, OrderDay order, CampDay camp) {
        this.date = date;
        this.adClicks = camp.getResults();
        this.orders = order.getOrders();
        this.adSpent = camp.getSpent();
        this.revenue = order.getCommission();
        this.cpc = adClicks != 0 ? adSpent.divide(BigDecimal.valueOf(adClicks), new MathContext(2)).floatValue() : 0f;
        this.conversionRate = orders != 0 ? adSpent.divide(BigDecimal.valueOf(orders), new MathContext(2)) : BigDecimal.ZERO;
        this.profit = revenue.subtract(adSpent);
        this.roas = revenue.divide(adSpent, new MathContext(2));
    }
}
