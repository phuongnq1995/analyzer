package org.phuongnq.analyzer.query.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CampaignEfficiency {
    public LocalDate date;
    @JsonPropertyDescription("Campaign name or id")
    public String name;
    @JsonPropertyDescription("Ad click")
    public int clicks;
    @JsonPropertyDescription("Number of product ordered")
    public int orders;
    @JsonPropertyDescription("Ad spent amount")
    public BigDecimal spent;
    @JsonPropertyDescription("Revenue")
    public BigDecimal commission;
    @JsonPropertyDescription("Cost per click")
    public float cpc;
    @JsonPropertyDescription("ConversionRate")
    public BigDecimal conversionRate;
    @JsonPropertyDescription("Profit amount")
    public BigDecimal revenue;
}
