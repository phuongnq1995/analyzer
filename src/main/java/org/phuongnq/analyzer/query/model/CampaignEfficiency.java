package org.phuongnq.analyzer.query.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignEfficiency {
    private LocalDate date;
    private BigDecimal netProfit;

    public CampaignEfficiency(LocalDate date, String name, int clicks, int orders, BigDecimal spent,
        BigDecimal commission) {
        this.date = date;
        this.name = name;
        this.clicks = clicks;
        this.orders = orders;
        this.spent = spent;
        this.commission = commission;
        generateData();
    }

    @JsonPropertyDescription("Campaign name or id")
    public String name;
    @JsonPropertyDescription("Ad click")
    public int clicks;
    @JsonPropertyDescription("Number of product ordered")
    public int orders;
    @JsonPropertyDescription("Ad spent amount")
    public BigDecimal spent;
    @JsonPropertyDescription("Revenue")
    public BigDecimal commission = BigDecimal.ZERO;
    @JsonPropertyDescription("Cost per click")
    public float cpc;
    @JsonPropertyDescription("ConversionRate")
    public BigDecimal conversionRate;
    @JsonPropertyDescription("Profit amount")
    public BigDecimal revenue;
    @JsonPropertyDescription("Return as Ad spend")
    public float roas;

    public CampaignEfficiency(CampDay campDay, OrderDay orderDay) {
        this.date = campDay.getDate();
        this.clicks = campDay.getResults();
        this.spent = campDay.getSpent();
        this.name = StringUtils.isEmpty(orderDay.getName()) ? "Others" : orderDay.getName();
        this.orders = orderDay.getOrders();
        this.commission = orderDay.getCommission();

        generateData();
    }

    public void generateData() {
        this.cpc = clicks != 0 ? spent.divide(BigDecimal.valueOf(clicks), new MathContext(2)).floatValue() : 0f;
        this.conversionRate = orders != 0 ? spent.divide(BigDecimal.valueOf(orders), new MathContext(2)) : BigDecimal.ZERO;
        this.revenue = commission.subtract(spent);
        this.roas = spent.compareTo(BigDecimal.ZERO) == 0 ? 0f : revenue.divide(spent, new MathContext(2)).floatValue();
    }
}
