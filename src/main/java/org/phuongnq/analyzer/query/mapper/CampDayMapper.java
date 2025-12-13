package org.phuongnq.analyzer.query.mapper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.phuongnq.analyzer.query.model.CampDay;
import org.springframework.jdbc.core.RowMapper;

public class CampDayMapper implements RowMapper<CampDay> {

    @Override
    public CampDay mapRow(ResultSet rs, int rowNum) throws SQLException {
        LocalDate date = rs.getDate("date").toLocalDate();
        String name = rs.getString("name");
        int results = rs.getInt("results");
        BigDecimal spent = rs.getBigDecimal("spent");

        return new CampDay(date, name, results, spent);
    }
}
