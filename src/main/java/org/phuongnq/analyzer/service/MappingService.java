package org.phuongnq.analyzer.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.Nullable;
import org.phuongnq.analyzer.dto.info.ShopSettings;
import org.phuongnq.analyzer.repository.CampaignRepository;
import org.phuongnq.analyzer.repository.OrderLinkRepository;
import org.phuongnq.analyzer.repository.entity.Campaign;
import org.phuongnq.analyzer.repository.entity.OrderLink;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.utils.NormalizerUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MappingService {
    private final CampaignRepository campaignRepository;
    private final OrderLinkRepository orderLinkRepository;
    private final UserService userService;

    @Transactional
    public void mappingSameNameCampaignsToOrderLinks(Long sId, List<Campaign> campaigns) {

        if (campaigns.isEmpty()) {
            return;
        }

        Set<String> campaignNames = campaigns.stream()
            .map(Campaign::getName)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());

        List<OrderLink> orderLinks = orderLinkRepository.findByShopIdAndSubIdIn(sId, campaignNames);

        if (orderLinks.isEmpty()) {
            return;
        }

        Map<String, OrderLink> orderLinkNameIdMap = orderLinks.stream()
            .collect(Collectors.toMap(OrderLink::getSubId, Function.identity()));

        mappingCampaignNames(campaigns, orderLinkNameIdMap);
    }

    @Transactional
    public void mappingSameOrderSubIdsToCampaigns(Long sId, List<OrderLink> newOrderLinks) {
        if (newOrderLinks.isEmpty()) {
            return;
        }

        Set<String> subIds = newOrderLinks.stream()
            .map(OrderLink::getSubId)
            .collect(Collectors.toSet());

        List<Campaign> campaigns = campaignRepository.findByShopIdAndNormalizedNameIn(sId, subIds);

        if (campaigns.isEmpty()) {
            return;
        }

        Map<String, Campaign> campaignMap = campaigns.stream()
            .collect(Collectors.toMap(Campaign::getName, Function.identity()));

        mappingOrderSubIds(newOrderLinks, campaignMap);
    }

    private void mappingOrderSubIds(List<OrderLink> newOrderLinks, Map<String, Campaign> campaignMap) {

        List<OrderLink> orderLinks = new ArrayList<>();

        for (OrderLink orderLink : newOrderLinks) {

            Campaign campaign = campaignMap.get(orderLink.getSubId());

            if (campaign != null && campaign.isUnmapped()) {
                orderLink.addCampaign(campaign);

                orderLinks.add(orderLink);
            }
        }

        if (CollectionUtils.isNotEmpty(orderLinks)) {
            orderLinkRepository.saveAll(orderLinks);
        }
    }

    private void mappingCampaignNames(List<Campaign> campaigns, Map<String, OrderLink> orderLinkNameIdMap) {
        List<Campaign> updateCampaigns = new ArrayList<>();

        for (Campaign campaign : campaigns) {

            OrderLink orderLink = orderLinkNameIdMap.get(campaign.getName());

            if (orderLink != null) {
                campaign.setOrderLink(orderLink);
                campaign.setUnmapped(true);

                updateCampaigns.add(campaign);
            }
        }

        if (CollectionUtils.isNotEmpty(updateCampaigns)) {
            campaignRepository.saveAll(updateCampaigns);
        }
    }

    @Transactional
    public List<Campaign> upsertCampaignNames(Long sid, Set<String> campaignNames) {

        Map<String, String> campaignNameMap = campaignNames.stream()
            .collect(Collectors.toMap(Function.identity(), NormalizerUtils::normalizeName));

        List<Campaign> campaigns = campaignRepository.findByShopIdAndNormalizedNameIn(sid, campaignNameMap.values());

        Set<String> newCampaignNames = new HashSet<>(campaignNames);
        newCampaignNames.removeAll(campaigns.stream()
            .map(Campaign::getName)
            .collect(Collectors.toSet()));

        if (newCampaignNames.isEmpty()) {
            return Collections.emptyList();
        }

        Shop shop = userService.getCurrentShop();

        List<Campaign> newCampaigns = newCampaignNames.stream()
            .map(campName -> Campaign.builder()
                .name(campName)
                .normalizedName(NormalizerUtils.normalizeName(campName))
                .shop(shop)
                .unmapped(false)
                .build())
            .collect(Collectors.toList());

        return campaignRepository.saveAll(newCampaigns);
    }

    @Transactional
    public List<OrderLink> upsertOrderSubIds(Long sid, Set<String> subIds) {
        List<OrderLink> orderLinks = orderLinkRepository.findByShopIdAndSubIdIn(sid, subIds);

        Set<String> newOrderSubIds = new HashSet<>(subIds);
        newOrderSubIds.removeAll(orderLinks.stream()
            .map(OrderLink::getSubId)
            .collect(Collectors.toSet()));

        if (newOrderSubIds.isEmpty()) {
            return Collections.emptyList();
        }

        Shop shop = userService.getCurrentShop();

        List<OrderLink> newOrderLinks = newOrderSubIds.stream()
            .map(subId -> OrderLink.builder()
                .subId(subId)
                .shop(shop)
                .build())
            .collect(Collectors.toList());

        return orderLinkRepository.saveAll(newOrderLinks);
    }

    public void getCampaigns() {

    }
}
