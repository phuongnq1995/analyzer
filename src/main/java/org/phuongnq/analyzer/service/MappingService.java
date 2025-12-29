package org.phuongnq.analyzer.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.phuongnq.analyzer.dto.shop.CampaignDto;
import org.phuongnq.analyzer.dto.shop.OrderLinkDto;
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
@Slf4j
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
            .filter(Campaign::isUnmapped)
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
    public void mappingSameOrderSubIdsToCampaigns(Long sId, List<OrderLink> orderLinks) {
        if (orderLinks.isEmpty()) {
            return;
        }

        Set<String> subIds = orderLinks.stream()
            .map(OrderLink::getSubId)
            .collect(Collectors.toSet());

        List<Campaign> campaigns = campaignRepository.findUnmappedByShopIAndNormalizedNameIn(sId, subIds);

        if (campaigns.isEmpty()) {
            return;
        }

        Map<String, Campaign> campaignMap = campaigns.stream()
            .collect(Collectors.toMap(Campaign::getName, Function.identity()));

        mappingOrderSubIds(orderLinks, campaignMap);
    }

    private void mappingOrderSubIds(List<OrderLink> orderLinks, Map<String, Campaign> campaignMap) {

        List<OrderLink> changedOrderLinks = new ArrayList<>();

        for (OrderLink orderLink : orderLinks) {

            Campaign campaign = campaignMap.get(orderLink.getSubId());

            if (campaign != null) {
                orderLink.addCampaign(campaign);

                changedOrderLinks.add(orderLink);
            }
        }

        if (CollectionUtils.isNotEmpty(changedOrderLinks)) {
            orderLinkRepository.saveAll(changedOrderLinks);
        }
    }

    private void mappingCampaignNames(List<Campaign> campaigns, Map<String, OrderLink> orderLinkNameIdMap) {
        List<Campaign> updateCampaigns = new ArrayList<>();

        for (Campaign campaign : campaigns) {

            OrderLink orderLink = orderLinkNameIdMap.get(campaign.getName());

            if (orderLink != null) {
                campaign.setOrderLink(orderLink);
                campaign.setUnmapped(false);

                updateCampaigns.add(campaign);
            }
        }

        if (CollectionUtils.isNotEmpty(updateCampaigns)) {
            campaignRepository.saveAll(updateCampaigns);

            log.info("Mapped {} campaign to link", updateCampaigns.size());
        }
    }

    @Transactional
    public List<Campaign> upsertCampaignNames(Long sid, Set<String> campaignNames) {

        Map<String, String> campaignNameMap = campaignNames.stream()
            .collect(Collectors.toMap(Function.identity(), NormalizerUtils::normalizeName));

        List<Campaign> campaigns = campaignRepository.findByShopIdAndNormalizedNameIn(sid, campaignNameMap.values());

        List<Campaign> allCampaigns = new ArrayList<>(campaigns);

        Set<String> newCampaignNames = new HashSet<>(campaignNames);
        newCampaignNames.removeAll(campaigns.stream()
            .map(Campaign::getName)
            .collect(Collectors.toSet()));

        if (!newCampaignNames.isEmpty()) {

            Shop shop = userService.getCurrentShop();

            List<Campaign> newCampaigns = newCampaignNames.stream()
                .map(campName -> Campaign.builder()
                    .name(campName)
                    .normalizedName(NormalizerUtils.normalizeName(campName))
                    .shop(shop)
                    .unmapped(true)
                    .build())
                .collect(Collectors.toList());

            newCampaigns = campaignRepository.saveAll(newCampaigns);

            log.info("Shop {}, inserted new {} campaigns", sid, newCampaigns.size());

            allCampaigns.addAll(newCampaigns);
        }

        return allCampaigns;
    }

    @Transactional
    public List<OrderLink> upsertOrderSubIds(Long sid, Set<String> subIds) {
        List<OrderLink> orderLinks = orderLinkRepository.findByShopIdAndSubIdIn(sid, subIds);

        List<OrderLink> allLinks = new ArrayList<>(orderLinks);

        Set<String> newOrderSubIds = new HashSet<>(subIds);
        newOrderSubIds.removeAll(orderLinks.stream()
            .map(OrderLink::getSubId)
            .collect(Collectors.toSet()));

        if (!newOrderSubIds.isEmpty()) {
            Shop shop = userService.getCurrentShop();

            List<OrderLink> newOrderLinks = newOrderSubIds.stream()
                .map(subId -> OrderLink.builder()
                    .subId(subId)
                    .shop(shop)
                    .build())
                .collect(Collectors.toList());

            newOrderLinks = orderLinkRepository.saveAll(newOrderLinks);

            log.info("Shop {}, inserted new {} order links.", sid, newOrderLinks.size());

            allLinks.addAll(newOrderLinks);
        }

        return allLinks;
    }

    @Transactional(readOnly = true)
    public List<OrderLink> getOrderLinks(Shop shop) {
        List<OrderLink> orderLinks = orderLinkRepository.getAll(shop);

        List<Campaign> unmappedCampaigns = getUnmappedCampaigns(shop);

        boolean existedOther = false;
        final ListIterator<OrderLink> li = orderLinks.listIterator();
        while (li.hasNext()) {
            OrderLink orderLink = li.next();
            if (orderLink.getSubId().equals("")) {
                li.set(new OrderLink(-1L, "", shop, Set.copyOf(unmappedCampaigns)));
                existedOther = true;
                break;
            }
        }

        if (!existedOther) {
            orderLinks.add(new OrderLink(-1L, "", shop, Set.copyOf(unmappedCampaigns)));
        }

        return orderLinks;
    }

    @Transactional(readOnly = true)
    public List<CampaignDto> getCampaigns() {
        Shop shop = userService.getCurrentShop();
        return campaignRepository.getAll(shop);
    }

    @Transactional(readOnly = true)
    public List<Campaign> getUnmappedCampaigns(Shop shop) {
        return campaignRepository.getUnmappedCampaigns(shop);
    }

    @Transactional(readOnly = true)
    public List<OrderLinkDto> getOrderLinks() {
        Shop shop = userService.getCurrentShop();

        return orderLinkRepository.getAll(shop).stream()
            .filter(entity -> StringUtils.isNotEmpty(entity.getSubId()))
            .map(entity -> {
                Collection<CampaignDto> campaignDtos = entity.getCampaigns().stream()
                    .map(campaign -> new CampaignDto(campaign.getId(), campaign.getName(), false))
                    .toList();
                return new OrderLinkDto(entity.getId(), entity.getSubId(), campaignDtos);
            })
            .toList();
    }

    @Transactional
    public void updateMapping(Long campaignId, Long linkId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        OrderLink orderLink = orderLinkRepository.findById(linkId).orElseThrow();
        campaign.setOrderLink(orderLink);
        campaign.setUnmapped(false);
        campaignRepository.save(campaign);
    }

    @Transactional
    public void deleteCampaignMapping(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow();
        campaign.setOrderLink(null);
        campaign.setUnmapped(true);
        campaignRepository.save(campaign);
    }
}
