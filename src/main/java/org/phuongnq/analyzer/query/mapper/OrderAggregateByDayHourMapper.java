package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.phuongnq.analyzer.query.model.HourlyStat;
import org.springframework.jdbc.core.RowMapper;

public class OrderAggregateByDayHourMapper implements RowMapper<HourlyStat> {

    @Override
    public HourlyStat mapRow(ResultSet rs, int rowNum) throws SQLException {
        LocalDate date = rs.getDate("dt").toLocalDate();
        int hour = rs.getInt("hr");
        int orders = rs.getInt("orders");
        BigDecimal commission = rs.getBigDecimal("commission");
        BigDecimal revenue = rs.getBigDecimal("revenue");
        return new HourlyStat(date, hour, orders, commission, revenue);
    }
}
