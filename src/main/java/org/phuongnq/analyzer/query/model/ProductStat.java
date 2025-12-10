package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductStat {
    public String category;
    public double commissionRate;
    public BigDecimal totalCommission;
    public int totalOrders;

    public ProductStat(String category, double commissionRate, BigDecimal totalCommission, int totalOrders) {
        this.category = category;
        this.commissionRate = commissionRate;
        this.totalCommission = totalCommission != null ? totalCommission : BigDecimal.ZERO;
        this.totalOrders = totalOrders;
    }
}