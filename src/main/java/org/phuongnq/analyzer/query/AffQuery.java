package org.phuongnq.analyzer.query;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.aff.RecommendationCampaign;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.mapper.CampDayMapper;
import org.phuongnq.analyzer.query.mapper.OrderDayMapper;
import org.phuongnq.analyzer.query.mapper.RecommendationCampaignMapper;
import org.phuongnq.analyzer.query.model.CampDay;
import org.phuongnq.analyzer.query.model.CampaignDateEfficiency;
import org.phuongnq.analyzer.query.model.OrderDay;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AffQuery {

    private final JdbcClient jdbcClient;

    public List<CampaignDateEfficiency> getMarketingEfficiency(String name, LocalDate fromDate, LocalDate toDate) {
        long sid = 2;

        Map<LocalDate, OrderDay> orderDays = queryOrderByDay(sid, name, fromDate, toDate)
            .stream()
            .collect(Collectors.toMap(OrderDay::getDate, Function.identity()));
        Map<LocalDate, CampDay> campDays = queryCampByDay(sid, name, fromDate, toDate)
            .stream()
            .collect(Collectors.toMap(CampDay::getDate, Function.identity()));

        return fromDate.datesUntil(toDate)
            .map(date -> {
                OrderDay order = orderDays.getOrDefault(date, new OrderDay());
                CampDay camp = campDays.getOrDefault(date, new CampDay());
                return new CampaignDateEfficiency(date, order, camp);
            }).collect(Collectors.toList());
    }

    public List<OrderDay> queryOrderByDay(Long sid, @Nullable String campName, LocalDate fromDate, LocalDate toDate) {
        String sql = """
                SELECT (clickTime::date) AS date,
                    subId1 AS name,
                    COUNT(DISTINCT orderId) AS orders,
                    COALESCE(SUM(totalProductCommission),0) AS commission
                FROM orders
                WHERE sId = :sId AND clickTime >= :from AND clickTime < :to %s
                GROUP BY date, name
                ORDER BY date, name;
                """;

        Map<String, Object> params = new HashMap<>() {{
            put("sId", sid);
            put("from", fromDate.atStartOfDay());
            put("to", toDate.plusDays(1).atStartOfDay());
        }};

        String campNameCondition = "";

        if (campName != null) {
            campNameCondition = "AND name = :name";
            params.put("name", campName);
        }

        return jdbcClient.sql(sql.formatted(campNameCondition))
            .params(params)
            .query(new OrderDayMapper())
            .list();
    }

    public List<CampDay> queryCampByDay(Long sid, @Nullable String campName, LocalDate fromDate, LocalDate toDate) {
        String sql = """
            SELECT (date::date) AS date,
                lower(campaignName) AS name,
                COALESCE(SUM(results),0) AS results,
                COALESCE(SUM(amountSpent),0) AS spent
            FROM ads
            WHERE sId = :sId AND date >= :from AND date < :to %s
            GROUP BY date, name
            order by date, name
            """;

        Map<String, Object> params = new HashMap<>() {{
            put("sId", sid);
            put("from", fromDate.atStartOfDay());
            put("to", toDate.plusDays(1).atStartOfDay());
        }};

        String campNameCondition = "";

        if (campName != null) {
            campNameCondition = "AND name = :name";
            params.put("name", campName);
        }

        return jdbcClient.sql(sql.formatted(campNameCondition))
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

}
