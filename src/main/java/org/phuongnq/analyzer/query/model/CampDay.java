package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CampDay {
    public LocalDate date;
    public String name;
    public int results;
    public BigDecimal spent;
}