package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.phuongnq.analyzer.query.model.OrderDelay;
import org.springframework.jdbc.core.RowMapper;

public class OrderDelayMapper implements RowMapper<OrderDelay> {

    @Override
    public OrderDelay mapRow(ResultSet rs, int rowNum) throws SQLException {
        String name = rs.getString("name");
        LocalDate clickDate = rs.getDate("clickDate").toLocalDate();
        LocalDate orderDate = rs.getDate("orderDate").toLocalDate();
        int delayedOrders = rs.getInt("delayedOrders");
        BigDecimal delayedRevenue = rs.getBigDecimal("delayedRevenue");
        int delayDays = rs.getInt("delayDays");
        return new OrderDelay(name, clickDate, orderDate, delayedOrders, delayedRevenue, delayDays);
    }
}
