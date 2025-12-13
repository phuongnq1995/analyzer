package org.phuongnq.analyzer.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.aff.RecommendationCampaign;
import org.phuongnq.analyzer.query.AffQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserService userService;
    private final AffQuery query;

    @Transactional(readOnly = true)
    public List<RecommendationCampaign> getRecommendations() {
        Long sid = userService.getCurrentShopId();
        Optional<Long> id = query.getLatestRecommendation(sid);
        if (id.isEmpty()) {
            return Collections.emptyList();
        }
        return query.getRecommendationCampaigns(id.get());
    }
}
