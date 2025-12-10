// language: java
package org.phuongnq.analyzer.query;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.query.mapper.CampDayMapper;
import org.phuongnq.analyzer.query.mapper.CampaignEfficiencyMapper;
import org.phuongnq.analyzer.query.mapper.OrderAggregateByDayHourMapper;
import org.phuongnq.analyzer.query.mapper.OrderDayMapper;
import org.phuongnq.analyzer.query.mapper.ProductCategoryAggregateMapper;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.CampaignEfficiency;
import org.phuongnq.analyzer.query.model.HourlyStat;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.phuongnq.analyzer.query.model.ProductStat;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AggregationQuery {

    private final JdbcClient jdbcClient;

    @Tool(description = "Get marketing efficiency between two dates")
    public List<CampaignEfficiency> getMarketingEfficiency(String name, LocalDate fromDate, LocalDate toDate) {
        String sql = """
                select *,
                    CASE
                        WHEN clicks = 0 THEN 0
                        ELSE spent / clicks
                    END AS CPC,
                    CASE
                        WHEN clicks = 0 THEN 0
                        ELSE orders * 1.0 / clicks
                    END AS conversionRate,
                    commission - spent as revenue
                FROM (
                select
                    a.name,
                    a.date,
                    COALESCE(SUM(a.results),0) AS clicks,
                    COALESCE(SUM(o.orders),0) AS orders,
                    COALESCE(SUM(a.spent),0) AS spent,
                    COALESCE(SUM(o.commission),0) AS commission
                from ads_view a
                left join order_view o
                on a.name = o.name and a.date = clickDate
                WHERE date >= ? AND date < ?
                group by  a.date, a.name
                order by a.date, a.name
                )
                WHERE (spent > 0 OR commission > 0)
                """;

        List<Object> params = new ArrayList<>();
        params.add(fromDate.atStartOfDay());
        params.add(toDate.plusDays(1).atStartOfDay());

        if (!name.isBlank()) {
            sql += " AND name = ?";
            params.add(name);
        }

        return jdbcClient.sql(sql)
            .params(params.toArray())
            .query(new CampaignEfficiencyMapper()).list();
    }

    public List<HourlyStat> queryAggregateOrderByHourly(LocalDate startDate, LocalDate endDate) {
        return jdbcClient.sql("""
                SELECT
                    (orderTime::date) AS dt,
                    EXTRACT(HOUR FROM orderTime) AS hr,
                    COUNT(DISTINCT orderId) AS orders,
                    COALESCE(SUM(netAffiliateMarketingCommission),0) AS commission,
                    COALESCE(SUM(orderValue),0) AS revenue
                FROM affiliate_orders
                WHERE orderTime >= ? AND orderTime < ?
                GROUP BY dt, hr
                ORDER BY dt, hr;
                """)
            .param(startDate.atStartOfDay())
            .param(endDate.plusDays(1).atStartOfDay())
            .query(new OrderAggregateByDayHourMapper()).list();
    }

    public List<ProductStat> queryAggregateProductCategoryByHourly(LocalDate startDate, LocalDate endDate) {
        return jdbcClient.sql("""
                SELECT
                    COALESCE(globalCatL1,'Unknown') AS category,
                    AVG(commissionRateOnProduct) AS commission_rate,
                    COALESCE(SUM(netAffiliateMarketingCommission),0) AS total_commission,
                    COUNT(*) AS total_orders
                FROM affiliate_orders
                WHERE orderTime >= ? AND orderTime < ?
                GROUP BY category
                ORDER BY total_commission DESC
                """)
            .param(startDate.atStartOfDay())
            .param(endDate.plusDays(1).atStartOfDay())
            .query(new ProductCategoryAggregateMapper()).list();
    }

    public List<OrderDay> queryOrderByDay(LocalDate fromDate, LocalDate toDate) {
        return jdbcClient.sql("""
                SELECT (clickTime::date) AS date,
                    subId1 as name,
                    COUNT(DISTINCT orderId) AS orders,
                    COALESCE(SUM(netAffiliateMarketingCommission),0) AS commission
                FROM affiliate_orders
                WHERE clickTime >= ? AND clickTime < ?
                GROUP BY date, name
                order by date, name;
                """)
            .param(fromDate.atStartOfDay())
            .param(toDate.plusDays(1).atStartOfDay())
            .query(new OrderDayMapper()).list();
    }

    public List<CampDay> queryCampByDay(LocalDate fromDate, LocalDate toDate) {
        return jdbcClient.sql("""
                SELECT (date::date) AS date,
                    lower(campaignName) as name,
                    COALESCE(SUM(results),0) AS results,
                    COALESCE(SUM(amountSpent),0) AS spent
                FROM ads
                WHERE date >= ? AND date < ?
                GROUP BY date, name
                order by date, name
                """)
            .param(fromDate.atStartOfDay())
            .param(toDate.plusDays(1).atStartOfDay())
            .query(new CampDayMapper()).list();
    }
}
