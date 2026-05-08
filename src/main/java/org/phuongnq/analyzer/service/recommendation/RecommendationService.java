package org.phuongnq.analyzer.service.recommendation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.dto.aff.EvaluateCampaignDto;
import org.phuongnq.analyzer.dto.aff.RecommendationDto;
import org.phuongnq.analyzer.repository.EvaluateEfficiencyRepository;
import org.phuongnq.analyzer.repository.entity.EvaluateEfficiency;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserService userService;
    private final EvaluateEfficiencyRepository efficiencyRepository;

    @Transactional(readOnly = true)
    public RecommendationDto getRecommendation() {
        Shop shop = userService.getCurrentShop();

        Optional<EvaluateEfficiency> efficiencyOpt = efficiencyRepository.findTop1ByShopOrderByEvaluateDateDesc(shop);

        if (efficiencyOpt.isEmpty()) {
            return RecommendationDto.builder()
                .evaluateDate(LocalDate.now())
                .build();
        }

        EvaluateEfficiency latestEvaluate = efficiencyOpt.get();

        List<EvaluateCampaignDto> evaluateCampaigns = latestEvaluate.getCampaignEfficiencies()
            .stream()
            .map(EvaluateCampaignDto::new)
            .sorted((o1, o2) -> o2.getLevel().getValue() -  o1.getLevel().getValue())
            .toList();

        return RecommendationDto.builder()
            .evaluateDate(latestEvaluate.getEvaluateDate())
            .evaluateCampaigns(evaluateCampaigns)
            .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveEvaluateEfficiency(EvaluateEfficiency evaluateEfficiency) {
        efficiencyRepository.save(evaluateEfficiency);
    }
}
