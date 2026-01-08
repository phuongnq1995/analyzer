package org.phuongnq.analyzer.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.phuongnq.analyzer.query.model.DelayPeriod;

@Entity
@Table(name = "conversionCurvePercentages")
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversionCurvePercentage implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DelayPeriod period;

    private float orderPercentage = 0f;
    private float revenuePercentage = 0f;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sid")
    private Shop shop;

    public ConversionCurvePercentage(Shop shop, String name, DelayPeriod period) {
        this.shop = shop;
        this.name = StringUtils.isEmpty(name) ? "Others" : name;
        this.period = period;
    }

    public static ConversionCurvePercentage defaultValue() {
        ConversionCurvePercentage conversionCurvePercentage = new ConversionCurvePercentage();
        conversionCurvePercentage.setRevenuePercentage(100f);
        conversionCurvePercentage.setOrderPercentage(100f);
        return conversionCurvePercentage;
    }
}
