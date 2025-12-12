package org.phuongnq.analyzer.dto.req;

import java.time.LocalDate;
import lombok.Data;

@Data
public class DateRange {
    private LocalDate fromDate;
    private LocalDate toDate;
}
