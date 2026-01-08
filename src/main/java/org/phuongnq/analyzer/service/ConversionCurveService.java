package org.phuongnq.analyzer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.model.ConversionCurve;
import org.phuongnq.analyzer.query.model.DelayPeriod;
import org.phuongnq.analyzer.repository.ConversionCurvePercentageRepository;
import org.phuongnq.analyzer.repository.entity.ConversionCurvePercentage;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.utils.MathUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversionCurveService {

    private final ConversionCurvePercentageRepository repository;
    private final AffQuery affQuery;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ingestConversionCurves(Shop shop) {
        Long sid = shop.getId();

        LocalDate considerDate = LocalDate.now().minusDays(4);
        Map<String, List<ConversionCurve>> conversionCurves = affQuery.getConversionCurve(sid, considerDate).stream()
            .collect(Collectors.groupingBy(ConversionCurve::getName));

        List<ConversionCurvePercentage> conversionCurvePercentages = new ArrayList<>();

        for (Map.Entry<String, List<ConversionCurve>> entry : conversionCurves.entrySet()) {
            String name = entry.getKey();
            List<ConversionCurve> values = entry.getValue();

            Map<Integer, ConversionCurvePercentage> curvePercentageMap = Map.of(
                0, new ConversionCurvePercentage(shop, name, DelayPeriod.SAME_DAY),
                1, new ConversionCurvePercentage(shop, name, DelayPeriod.ONE_DAY),
                2, new ConversionCurvePercentage(shop, name, DelayPeriod.TWO_DAYS)
            );

            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalOrder = 0;

            for (ConversionCurve curve : values) {
                totalRevenue = totalRevenue.add(curve.getRevenue());
                totalOrder += curve.getOrders();
            }

            if (MathUtils.isZero(totalRevenue) || totalOrder == 0) {
                continue;
            }

            values.sort(Comparator.comparingInt(ConversionCurve::getDelay));

            BigDecimal cumulativeRevenue = BigDecimal.ZERO;
            int cumulativeOrders = 0;

            for (ConversionCurve curve : values) {
                if (curve.getDelay() > 2) {
                    break;
                }
                cumulativeRevenue = cumulativeRevenue.add(curve.getRevenue());
                cumulativeOrders += curve.getOrders();

                ConversionCurvePercentage conversionCurvePercentage = curvePercentageMap.get(curve.getDelay());
                conversionCurvePercentage.setOrderPercentage(MathUtils.toPercentageOf(cumulativeOrders, totalOrder));
                conversionCurvePercentage.setRevenuePercentage(MathUtils.toPercentageOf(cumulativeRevenue, totalRevenue));
            }

            conversionCurvePercentages.addAll(curvePercentageMap.values());
        }

        repository.deleteAll();
        repository.saveAll(conversionCurvePercentages);
    }

}
