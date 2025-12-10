package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderDay {
    public LocalDate date;
    public String name;
    public int orders;
    public BigDecimal commission;
}