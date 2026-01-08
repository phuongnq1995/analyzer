package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.phuongnq.analyzer.query.model.ConversionCurve;
import org.springframework.jdbc.core.RowMapper;

public class OrderDelayMapper implements RowMapper<ConversionCurve> {

    @Override
    public ConversionCurve mapRow(ResultSet rs, int rowNum) throws SQLException {
        String name = rs.getString("name");
        int delay = rs.getInt("delay");
        int orders = rs.getInt("orders");
        BigDecimal revenue = rs.getBigDecimal("revenue");

        return new ConversionCurve(name, delay, orders, revenue);
    }
}
