package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDay {
    private LocalDate date;
    private String name;
    private int orders = 0;
    private BigDecimal commission = BigDecimal.ZERO;
}