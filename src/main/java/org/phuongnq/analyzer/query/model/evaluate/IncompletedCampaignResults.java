package org.phuongnq.analyzer.query.model.evaluate;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.utils.MathUtils;

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
    private float estimateConversionRate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal estimateRevenue;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.00")
    private BigDecimal estimateNetProfit;
    private float estimateRoas;

    public IncompletedCampaignResults(EvaluateCampaign efficiency, Shop shop) {
        this.date = efficiency.getDate();
        this.clicks = efficiency.getClicks();
        this.currentOrders = efficiency.getOrders();
        this.spent = efficiency.getSpent();
        this.cpc = efficiency.getCpc();
        this.currentRevenue = efficiency.getRevenue();

        this.estimateOrders = MathUtils.withPercentage(currentOrders, efficiency.getOrderPercentage());
        this.estimateRevenue = MathUtils.withPercentageBigDecimal(currentRevenue, efficiency.getRevenuePercentage());
        this.estimateConversionRate = clicks != 0 ? (float) estimateOrders / clicks: 0f;
        this.estimateNetProfit = calNetProfit(shop, estimateRevenue, spent);
        this.estimateRoas = spent.compareTo(BigDecimal.ZERO) == 0 ? 0f : estimateRevenue.divide(spent, new MathContext(2)).floatValue();
    }

    private BigDecimal calNetProfit(Shop shop, BigDecimal commission, BigDecimal spent) {
        BigDecimal netCommission = MathUtils.isPositive(commission) ? commission.multiply(BigDecimal.ONE.subtract(shop.getSalesTax())) : BigDecimal.ZERO;
        BigDecimal netSpent = MathUtils.isPositive(spent) ? spent.multiply(BigDecimal.ONE.add(shop.getMarketingFee())) : BigDecimal.ZERO;
        return netCommission.subtract(netSpent);
    }

}
