package org.phuongnq.analyzer.query.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignEfficiency implements Serializable {

    public CampaignEfficiency(String name, int clicks, int orders, BigDecimal spent, BigDecimal commission) {
        this.name = name;
        this.clicks = clicks;
        this.orders = orders;
        this.spent = spent;
        this.commission = commission;
        generateData();
    }

    private String name;
    private int clicks;
    private int orders;
    private BigDecimal spent;
    private BigDecimal commission = BigDecimal.ZERO;
    private float cpc;
    private float conversionRate;
    private BigDecimal revenue;
    private float roas;
    private BigDecimal netProfit;

    public CampaignEfficiency(CampDay campDay, OrderDay orderDay) {
        this.clicks = campDay.getResults();
        this.spent = campDay.getSpent();
        this.name = StringUtils.isEmpty(orderDay.getName()) ? "Others" : orderDay.getName();
        this.orders = orderDay.getOrders();
        this.commission = orderDay.getCommission();

        generateData();
    }

    public void generateData() {
        this.cpc = clicks != 0 ? spent.divide(BigDecimal.valueOf(clicks), new MathContext(2)).floatValue() : 0f;
        this.conversionRate = clicks != 0 ? (float) orders / clicks: 0f;
        this.revenue = commission.subtract(spent);
        this.roas = spent.compareTo(BigDecimal.ZERO) == 0 ? 0f : revenue.divide(spent, new MathContext(2)).floatValue();
    }
}
