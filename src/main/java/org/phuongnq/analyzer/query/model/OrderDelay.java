package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDelay {
    private String name;
    private LocalDate clickDate;
    private LocalDate orderDate;
    private int orders;
    private BigDecimal revenue;
    private int delayDays;
}
