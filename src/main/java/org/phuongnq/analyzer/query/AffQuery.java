package org.phuongnq.analyzer.query;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.aff.RecommendationCampaign;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.mapper.CampDayMapper;
import org.phuongnq.analyzer.query.mapper.OrderDayMapper;
import org.phuongnq.analyzer.query.mapper.OrderDelayMapper;
import org.phuongnq.analyzer.query.mapper.RecommendationCampaignMapper;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.phuongnq.analyzer.query.model.OrderDelay;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
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

    public Optional<Long> getLatestRecommendation(Long sId) {
        String sql = """
            SELECT id
            FROM recommendation
            WHERE sId = :sId
            ORDER BY finishedTime DESC
            FETCH FIRST 1 ROW ONLY
            """;

        return jdbcClient.sql(sql)
            .param("sId", sId)
            .query(Long.class)
            .optional();
    }

    public List<RecommendationCampaign> getRecommendationCampaigns(Long recommendationId) {
        String sql = """
            SELECT id, campaignName, efficiencyLevel, action, advise
            FROM recommendation_campaign
            WHERE recommendationId = :recommendationId
            """;

        return jdbcClient.sql(sql)
            .param("recommendationId", recommendationId)
            .query(new RecommendationCampaignMapper())
            .list();
    }

    public List<OrderDelay> getDistributeOrder(Long sId, LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT
                 subId1 AS name,
                 (clickTime::date) AS clickDate,
                 (orderTime::date) AS orderDate,
                 COUNT(DISTINCT orderId) AS delayedOrders,
                 COALESCE(SUM(netAffiliateMarketingCommission),0) AS delayedRevenue,
                 (orderTime::date) - (clickTime::date) AS delayDays
               FROM orders
               WHERE orderStatus != 'Đã hủy' AND sId = :sId
                AND (clickTime::date) >= :from AND (clickTime::date) < :to
               GROUP BY
                 clickDate, orderDate, subId1
               ORDER BY orderDate, clickDate
               """;

        Map<String, Object> params = new HashMap<>() {{
            put("sId", sId);
            put("from", fromDate.atStartOfDay());
            put("to", toDate.plusDays(1).atStartOfDay());
        }};

        return jdbcClient.sql(sql)
            .params(params)
            .query(new OrderDelayMapper())
            .list();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshOrderData() {
        jdbcClient.sql("REFRESH MATERIALIZED VIEW mv_orders_by_click_date").update();
        jdbcClient.sql("REFRESH MATERIALIZED VIEW mv_orders_by_order_date").update();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void refreshAdsData() {
        jdbcClient.sql("REFRESH MATERIALIZED VIEW mv_ads_date").update();
    }
}
