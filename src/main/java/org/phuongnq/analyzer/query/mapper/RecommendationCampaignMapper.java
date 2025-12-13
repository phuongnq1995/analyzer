package org.phuongnq.analyzer.query.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.phuongnq.analyzer.dto.aff.RecommendationCampaign;
import org.springframework.jdbc.core.RowMapper;

public class RecommendationCampaignMapper implements RowMapper<RecommendationCampaign> {

    @Override
    public RecommendationCampaign mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("campaignName");
        int efficiencyLevel = rs.getInt("efficiencyLevel");
        String action = rs.getString("action");
        String advice = rs.getString("advice");

        return new RecommendationCampaign(id, name, efficiencyLevel, action, advice);
    }
}
