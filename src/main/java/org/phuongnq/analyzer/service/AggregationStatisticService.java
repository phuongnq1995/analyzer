package org.phuongnq.analyzer.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.phuongnq.analyzer.repository.entity.Campaign;
import org.phuongnq.analyzer.repository.entity.OrderLink;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.utils.MathUtils;
import org.phuongnq.analyzer.utils.NormalizerUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AggregationStatisticService {

    private final AffQuery affQuery;
    private final UserService service;
    private final MappingService mappingService;
    private final CacheService cacheService;

    public List<AggregationByDateResult> getAggregationStatistics(LocalDate from, LocalDate to, String type) {
        Instant start = Instant.now();

        Long sId = service.getCurrentShopId();

        List<AggregationByDateResult> aggregation;

        if (type.equals("clickTime")) {
            aggregation = cacheService.getAggregateByClick(sId, from, to);
        } else {
            aggregation = cacheService.getAggregateByOrder(sId, from, to);
        }

        log.info("Shop {}: Fetched aggregation[{}] from {} to {}, in {} ms",
            sId, type, from, to, Duration.between(start, Instant.now()).toMillis());

        return aggregation;
    }

    public void cacheAggregates(Shop shop, DateRange input) {
        Instant start = Instant.now();

        List<AggregationByDateResult> aggregationByClickTimes = calAggregationStatistics(shop, input.getFromDate(),
            input.getToDate(), "clickTime");

        cacheService.cacheAggregateByClick(shop.getId(), input.getFromDate(), input.getToDate(), aggregationByClickTimes);

        List<AggregationByDateResult> aggregationByOrderTimes = calAggregationStatistics(shop, input.getFromDate(),
            input.getToDate(), "orderTime");

        cacheService.cacheAggregateByOrder(shop.getId(), input.getFromDate(), input.getToDate(), aggregationByOrderTimes);

        log.info("Shop {}: Cached aggregation from {} to {}, in {} ms",
            shop.getId(), input.getFromDate(), input.getToDate(), Duration.between(start, Instant.now()).toMillis());
    }

    public List<AggregationByDateResult> calAggregationStatistics(Shop shop, LocalDate fromDate, LocalDate toDate, String type) {
        Instant start = Instant.now();
        List<AggregationByDateResult> aggregationResults = new ArrayList<>();

        Long sid = shop.getId();

        List<CampDay> campByDay = affQuery.queryCampByDay(sid, fromDate, toDate);
        List<OrderDay> orderByDay = affQuery.queryOrderByDay(sid, type, fromDate, toDate);

        Map<LocalDate, List<CampDay>> campDayMap = campByDay.stream()
            .collect(Collectors.groupingBy(CampDay::getDate));
        Map<LocalDate, List<OrderDay>> orderDayMap = orderByDay.stream()
            .collect(Collectors.groupingBy(OrderDay::getDate));

        List<OrderLink> orderLinks = mappingService.getOrderLinks(shop);

        LocalDate date = fromDate;
        while (!date.isAfter(toDate)) {

            Map<String, CampDay> campMap = campDayMap.getOrDefault(date, Collections.emptyList())
                .stream()
                .collect(
                    Collectors.toMap(campDay -> NormalizerUtils.normalizeName(campDay.getName()), Function.identity()));

            Map<String, OrderDay> orderMap = orderDayMap.getOrDefault(date, Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(OrderDay::getName, Function.identity()));

            AggregationByDateResult aggregationByDateResult = mappingEfficiencyByDate(orderLinks, campMap, orderMap, date, shop);

            aggregationResults.add(aggregationByDateResult);

            date = date.plusDays(1);
        }

        log.info("Shop {}: Fetched aggregation statistics from {} to {}, in {} ms",
            sid, fromDate, toDate, Duration.between(start, Instant.now()).toMillis());

        return aggregationResults;
    }

    private AggregationByDateResult mappingEfficiencyByDate(List<OrderLink> orderLinks, Map<String, CampDay> campMap,
        Map<String, OrderDay> orderMap, LocalDate date, Shop shop) {

        List<CampaignEfficiency> campaignEfficiencies = new ArrayList<>();
        AggregationByDateResult result = new AggregationByDateResult(date, campaignEfficiencies);

        for (OrderLink orderLink : orderLinks) {

            Optional<CampDay> totalCampDay = orderLink.getCampaigns().stream()
                .map(Campaign::getNormalizedName)
                .filter(name -> campMap.containsKey(name) && campMap.get(name) != null)
                .map(campMap::get)
                .reduce((campDay, campDay2) -> {
                    campDay.setSpent(campDay.getSpent().add(campDay2.getSpent()));
                    campDay.setResults(campDay.getResults() + campDay2.getResults());
                    campDay.setName(StringUtils.isEmpty(orderLink.getSubId()) ? "Others" : orderLink.getSubId());
                    return campDay;
                });

            if (orderMap.containsKey(orderLink.getSubId()) || totalCampDay.isPresent()) {

                CampDay campDay = totalCampDay.orElse(new CampDay(date, orderLink.getSubId(), 0, BigDecimal.ZERO));
                OrderDay orderDay = orderMap.getOrDefault(orderLink.getSubId(), new OrderDay(date, orderLink.getSubId(), 0, BigDecimal.ZERO));

                CampaignEfficiency efficiency = new CampaignEfficiency(campDay, orderDay);
                efficiency.setNetProfit(calNetProfit(shop, efficiency.getCommission(), efficiency.getSpent()));

                // Add mapped campaign
                campaignEfficiencies.add(efficiency);
            }
        }

        return result;
    }

    private BigDecimal calNetProfit(Shop shop, BigDecimal commission, BigDecimal spent) {
        BigDecimal netCommission = MathUtils.isPositive(commission) ? commission.multiply(BigDecimal.ONE.subtract(shop.getSalesTax())) : BigDecimal.ZERO;
        BigDecimal netSpent = MathUtils.isPositive(spent) ? spent.multiply(BigDecimal.ONE.add(shop.getMarketingFee())) : BigDecimal.ZERO;
        return netCommission.subtract(netSpent);
    }

}
