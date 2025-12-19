package org.phuongnq.analyzer.dto.info;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopSettings{
    String name;
    String description;
    float marketingFee;
    float salesTax;

    public ShopSettings(String name, String description, BigDecimal marketingFee, BigDecimal salesTax) {
        this.name = name;
        this.description = description;
        this.marketingFee = marketingFee.floatValue();
        this.salesTax = salesTax.floatValue();
    }
}
