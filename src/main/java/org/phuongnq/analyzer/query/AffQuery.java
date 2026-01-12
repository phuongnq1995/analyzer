package org.phuongnq.analyzer.query;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.mapper.CampDayMapper;
import org.phuongnq.analyzer.query.mapper.OrderDayMapper;
import org.phuongnq.analyzer.query.mapper.OrderDelayMapper;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.phuongnq.analyzer.query.model.ConversionCurve;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class AffQuery {

    private static final Map<String, String> TYPE_VIEW_MAPPING = new HashMap<>() {{
        put("clickTime", "mv_orders_by_click_date");
        put("orderTime", "mv_orders_by_order_date");
    }};

    private final JdbcClient jdbcClient;

    public List<OrderDay> queryOrderByDay(Long sid, String type, LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT date, name, orders, commission
                FROM %s
                WHERE sId = :sId AND date >= :from AND date < :to
                """.formatted(TYPE_VIEW_MAPPING.getOrDefault(type, "mv_orders_by_click_date"));

        Map<String, Object> params = new HashMap<>() {{
            put("sId", sid);
            put("from", fromDate.atStartOfDay());
            put("to", toDate.plusDays(1).atStartOfDay());
        }};

        return jdbcClient.sql(sql)
            .params(params)
            .query(new OrderDayMapper())
            .list();
    }

    public List<CampDay> queryCampByDay(Long sid, LocalDate fromDate, LocalDate toDate) {
        String sql = """
            SELECT date, name, results, spent
            FROM mv_ads_date
            WHERE sId = :sId AND date >= :from AND date < :to
            """;

        Map<String, Object> params = new HashMap<>() {{
            put("sId", sid);
            put("from", fromDate.atStartOfDay());
            put("to", toDate.plusDays(1).atStartOfDay());
        }};

        return jdbcClient.sql(sql)
            .params(params)
            .query(new CampDayMapper())
            .list();
    }

    public int cleanOrdersData(Long sid, DateRange input) {
        return jdbcClient.sql("""
            DELETE from orders
            WHERE sId = :sId AND orderTime >= :from AND orderTime < :to
            """)
            .param("sId", sid)
            .param("from", input.getFromDate().atStartOfDay())
            .param("to", input.getToDate().plusDays(1).atStartOfDay())
            .update();
    }

    public int cleanAdsData(Long sid, DateRange input) {
        return jdbcClient.sql("""
            DELETE from ads
            WHERE sId = :sId AND date >= :from AND date < :to
            """)
            .param("sId", sid)
            .param("from", input.getFromDate())
            .param("to", input.getToDate().plusDays(1))
            .update();
    }

    public List<ConversionCurve> getConversionCurve(Long sId, LocalDate date) {
        String sql = """
                SELECT name, delay,
                    COALESCE(SUM(orders), 0) AS orders,
                    COALESCE(SUM(revenue), 0) AS revenue
                FROM
                    (
                    SELECT subid1 AS name,
                        (orderTime::date) - (clickTime::date) AS delay,
                        COUNT(DISTINCT orderId) AS orders,
                        COALESCE(SUM(totalProductCommission), 0) AS revenue
                    FROM
                        orders
                    WHERE
                        sId = :sId AND (clickTime::date) <= :date AND orderStatus != 'Đã hủy'
                    GROUP BY
                        subId1,
                        (orderTime::date),
                        (clickTime::date)
                    ORDER BY delay
                )
                GROUP BY name, delay
                ORDER BY name, delay
               """;

        Map<String, Object> params = new HashMap<>() {{
            put("sId", sId);
            put("date", date);
        }};

        return jdbcClient.sql(sql)
            .params(params)
            .query(new OrderDelayMapper())
            .list();
    }

    public Map<String, Object> getLatestImportAndEvaluateRun(Long sid) {
        String sql = """
            SELECT
            (SELECT MAX(orderTime::date) FROM orders WHERE sId = :sid) AS latestOrderImport,
            (SELECT MAX(date) FROM ads WHERE sid = :sid) AS latestAdsImport,
            (SELECT MAX(createdTime::date) FROM evaluateCampaignEfficiency WHERE sId = :sid) AS latestEvaluation
            """;
        return jdbcClient.sql(sql)
            .param("sid", sid)
            .query()
            .singleRow();
    }

    @Transactional
    public void refreshOrderData() {
        jdbcClient.sql("REFRESH MATERIALIZED VIEW mv_orders_by_click_date").update();
        jdbcClient.sql("REFRESH MATERIALIZED VIEW mv_orders_by_order_date").update();
    }

    @Transactional
    public void refreshAdsData() {
        jdbcClient.sql("REFRESH MATERIALIZED VIEW mv_ads_date").update();
    }
}
