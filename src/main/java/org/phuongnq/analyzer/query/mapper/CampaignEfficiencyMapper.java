package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.springframework.jdbc.core.RowMapper;

public class CampaignEfficiencyMapper implements RowMapper<CampaignEfficiency> {

    @Override
    public CampaignEfficiency mapRow(ResultSet rs, int rowNum) throws SQLException {
        LocalDate date = rs.getDate("date").toLocalDate();
        String name = rs.getString("name");
        int clicks = rs.getInt("clicks");
        int orders = rs.getInt("orders");
        BigDecimal spent = rs.getBigDecimal("spent");
        BigDecimal commission = rs.getBigDecimal("commission");
        float cpc = rs.getFloat("cpc");
        BigDecimal conversionRate = rs.getBigDecimal("conversionRate");
        BigDecimal revenue = rs.getBigDecimal("revenue");

        return new CampaignEfficiency(date, name, clicks, orders, spent, commission, cpc, conversionRate, revenue);
    }
}
