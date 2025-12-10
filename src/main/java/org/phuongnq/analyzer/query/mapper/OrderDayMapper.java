package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.springframework.jdbc.core.RowMapper;

public class OrderDayMapper implements RowMapper<OrderDay> {

    @Override
    public OrderDay mapRow(ResultSet rs, int rowNum) throws SQLException {
        LocalDate date = rs.getDate("date").toLocalDate();
        String name = rs.getString("name");
        int orders = rs.getInt("orders");
        BigDecimal commission = rs.getBigDecimal("commission");

        return new OrderDay(date, name, orders, commission);
    }
}
