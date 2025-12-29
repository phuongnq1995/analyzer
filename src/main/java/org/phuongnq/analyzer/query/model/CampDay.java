package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampDay {
    public LocalDate date;
    public String name;
    public int results = 0;
    public BigDecimal spent = BigDecimal.ZERO;
}