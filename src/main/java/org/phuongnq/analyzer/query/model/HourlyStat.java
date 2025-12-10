package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class HourlyStat {
    private String id;
    public LocalDate date;
    public int hour;
    public int orders;
    public BigDecimal commission;
    public BigDecimal revenue;

    public HourlyStat(LocalDate date, int hour, int orders, BigDecimal commission, BigDecimal revenue) {
        this.id = date.toString() + "-" + hour;
        this.date = date;
        this.hour = hour;
        this.orders = orders;
        this.commission = commission != null ? commission : BigDecimal.ZERO;
        this.revenue = revenue != null ? revenue : BigDecimal.ZERO;
    }
}

