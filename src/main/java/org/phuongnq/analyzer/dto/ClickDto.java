package org.phuongnq.analyzer.dto;

import com.opencsv.bean.CsvDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ClickDto {
    String id;
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    LocalDateTime clickTime;
    String areaZone;
    String channel;
    String subIds;
}
