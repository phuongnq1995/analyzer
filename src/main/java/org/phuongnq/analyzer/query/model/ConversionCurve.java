package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversionCurve {
    private String name;
    private int delay;
    private int orders;
    private BigDecimal revenue;
}
