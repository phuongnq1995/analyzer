package org.phuongnq.analyzer.query.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversionPacingCurve {
    private Long id;
    private Long sId;
    private String name;
    private LocalDate date;
    private int delayDate;
    private BigDecimal revenue;
    private BigDecimal percentage;
}