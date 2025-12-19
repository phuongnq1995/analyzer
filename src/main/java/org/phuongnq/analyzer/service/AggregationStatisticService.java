package org.phuongnq.analyzer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.utils.MathUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AggregationStatisticService {

    private final AffQuery affQuery;
    private final UserService service;

    public List<AggregationByDateResult> getCompareAggregationStatistics(LocalDate fromDate, LocalDate toDate,
        String type) {
        // return result object
        log.info("Fetching aggregation statistics from {} to {}", fromDate, toDate);
        Shop currentShop = service.getCurrentShop();
        Long sid = currentShop.getId();

        List<CampDay> campByDay = affQuery.queryCampByDay(sid, fromDate, toDate);
        List<OrderDay> orderByDay = affQuery.queryOrderByDay(sid, type, fromDate, toDate);

        Set<String> campNames = campByDay.stream()
            .map(CampDay::getName)
            .collect(Collectors.toSet());

        Set<String> orderNames = orderByDay.stream()
            .map(OrderDay::getName)
            .collect(Collectors.toSet());

        String otherOrder = "";
        orderNames.remove(otherOrder);

        Set<String> otherCampaigns = new HashSet<>(campNames);
        otherCampaigns.removeAll(orderNames);

        Map<LocalDate, List<CampDay>> campDayMap = campByDay.stream()
            .collect(Collectors.groupingBy(CampDay::getDate));

        Map<LocalDate, List<OrderDay>> orderDayMap = orderByDay.stream()
            .collect(Collectors.groupingBy(OrderDay::getDate));

        List<AggregationByDateResult> aggregationResults = new ArrayList<>();

        LocalDate date = fromDate;
        while (!date.isAfter(toDate)) {
            List<CampaignEfficiency> campaignEfficiencies = new ArrayList<>();
            AggregationByDateResult result = new AggregationByDateResult();
            result.setDate(date);

            List<CampDay> campDays = campDayMap.getOrDefault(date, List.of());
            Map<String, OrderDay> orderDayNameMap = orderDayMap.getOrDefault(date, List.of())
                .stream()
                .collect(Collectors.toMap(OrderDay::getName, Function.identity()));

            CampaignEfficiency otherCompare = new CampaignEfficiency();
            otherCompare.setName("Other");
            otherCompare.setDate(date);

            int otherResults = 0;
            int otherOrders = 0;
            BigDecimal otherSpent = BigDecimal.ZERO;
            BigDecimal otherCommission = BigDecimal.ZERO;
            for (CampDay campDay : campDays) {
                if (otherCampaigns.contains(campDay.getName())) {
                    otherResults += campDay.getResults();
                    otherSpent = otherSpent.add(campDay.getSpent());
                } else {
                    OrderDay orderDay = orderDayNameMap.remove(campDay.getName());

                    CampaignEfficiency campaignEfficiency = new CampaignEfficiency(campDay, orderDay);

                    campaignEfficiency.setNetProfit(calNetProfit(currentShop, campaignEfficiency.getCommission(), campaignEfficiency.getSpent()));

                    // Add mapped campaign
                    campaignEfficiencies.add(campaignEfficiency);
                }
            }

            for (String remainingOrderDayName : orderDayNameMap.keySet()) {
                OrderDay orderDay = orderDayNameMap.get(remainingOrderDayName);

                otherOrders += orderDay.getOrders();
                otherCommission = otherCommission.add(orderDay.getCommission());
            }

            otherCompare.setClicks(otherResults);
            otherCompare.setSpent(otherSpent);
            otherCompare.setOrders(otherOrders);
            otherCompare.setCommission(otherCommission);
            otherCompare.setNetProfit(calNetProfit(currentShop, otherCompare.getCommission(), otherCompare.getSpent()));

            OrderDay orderDay = orderDayNameMap.get("");

            if (orderDay != null) {
                otherCompare.setOrders(orderDay.getOrders());
                otherCompare.setCommission(orderDay.getCommission());
            } else {
                otherCompare.setOrders(0);
                otherCompare.setCommission(BigDecimal.ZERO);
            }

            otherCompare.generateData();
            // Add other compare
            campaignEfficiencies.add(otherCompare);

            result.setCampaignEfficiencies(campaignEfficiencies);

            aggregationResults.add(result);

            date = date.plusDays(1);
        }

        return aggregationResults;
    }

    private BigDecimal calNetProfit(Shop shop, BigDecimal commission, BigDecimal spent) {
        BigDecimal netCommission = MathUtils.isPositive(commission) ? commission.multiply(BigDecimal.ONE.subtract(shop.getSalesTax())) : BigDecimal.ZERO;
        BigDecimal netSpent = MathUtils.isPositive(spent) ? spent.multiply(BigDecimal.ONE.add(shop.getMarketingFee())) : BigDecimal.ZERO;
        return netCommission.subtract(netSpent);
    }
}
