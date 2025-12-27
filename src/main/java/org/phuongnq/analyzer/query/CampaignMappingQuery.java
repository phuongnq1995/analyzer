package org.phuongnq.analyzer.query;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CampaignMappingQuery {

    private final JdbcClient jdbcClient;

    public List<String> queryCampaignNames(Long sId, Collection<String> campaignNames) {
        String sql = """
           SELECT DISTINCT name
           FROM mv_ads_date
           WHERE sId = :sId
           """;
        return jdbcClient.sql(sql)
            .param("sId", sId)
            .query((rs, rowNum) -> rs.getString("name"))
            .list();
    }

    public List<String> queryOrderSubIds(Long sId) {
        String sql = """
           SELECT DISTINCT subId AS name
           FROM orders
           WHERE sId = :sId
           """;
        return jdbcClient.sql(sql)
            .param("sId", sId)
            .query((rs, rowNum) -> rs.getString("name"))
            .list();
    }
}
