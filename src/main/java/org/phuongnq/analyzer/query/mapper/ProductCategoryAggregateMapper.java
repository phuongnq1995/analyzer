package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.phuongnq.analyzer.query.model.HourlyStat;
import org.phuongnq.analyzer.query.model.ProductStat;
import org.springframework.jdbc.core.RowMapper;

public class ProductCategoryAggregateMapper implements RowMapper<ProductStat> {

    @Override
    public ProductStat mapRow(ResultSet rs, int rowNum) throws SQLException {
        String category = rs.getString("category");
        double commissionRate = rs.getDouble("commission_rate");
        BigDecimal totalCommission = rs.getBigDecimal("total_commission");
        int totalOrders = rs.getInt("total_orders");
        return new ProductStat(category, commissionRate, totalCommission, totalOrders);
    }
}
