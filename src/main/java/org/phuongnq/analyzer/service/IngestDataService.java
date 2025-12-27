package org.phuongnq.analyzer.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.dto.aff.AdsDto;
import org.phuongnq.analyzer.dto.aff.OrderDto;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.BatchOperation;
import org.phuongnq.analyzer.query.CampaignMappingQuery;
import org.phuongnq.analyzer.repository.entity.Campaign;
import org.phuongnq.analyzer.repository.entity.OrderLink;
import org.phuongnq.analyzer.utils.CSVHelper;
import org.phuongnq.analyzer.utils.NormalizerUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestDataService {

    private final CSVHelper csvHelper;
    private final BatchOperation batchOperation;
    private final AffQuery affQuery;
    private final CampaignMappingQuery campaignMappingQuery;
    private final MappingService mappingService;
    private final UserService service;

    @Transactional
    public void ingestOrders(MultipartFile file, DateRange input) {
        Long sid = service.getCurrentShopId();
        int count = affQuery.cleanOrdersData(sid, input);
        List<OrderDto> orders = csvHelper.readOrderFromCsv(file);

        log.info("Deleted {} rows of orders from {} to {}", count, input.getFromDate(), input.getToDate());

        int insertCount = batchOperation.batchInsertOrUpdateOrders(sid, orders);

        affQuery.refreshOrderData();

        log.info("Inserted {} rows of orders from {} to {}", insertCount, input.getFromDate(), input.getToDate());

        Set<String> subIds = orders.stream()
            .map(OrderDto::getSubId1)
            .collect(Collectors.toSet());

        List<OrderLink> newOrderLinks = mappingService.upsertOrderSubIds(sid, subIds);

        log.info("Inserted {} rows of orderLink", newOrderLinks.size());

        mappingService.mappingSameOrderSubIdsToCampaigns(sid, newOrderLinks);
    }

    @Transactional
    public void ingestAds(MultipartFile file, DateRange input) {
        Long sid = service.getCurrentShopId();
        List<AdsDto> ads = csvHelper.readAdFromCsv(file);
        int count = affQuery.cleanAdsData(sid, input);
        log.info("Deleted {} rows of ads from {} to {}", count, input.getFromDate(), input.getToDate());

        int insertCount = batchOperation.batchInsertOrUpdateAds(sid, ads);

        affQuery.refreshAdsData();

        log.info("Inserted {} rows of ads from {} to {}", insertCount, input.getFromDate(), input.getToDate());

        Set<String> campaignNames = ads.stream()
            .map(AdsDto::getCampaignName)
            .collect(Collectors.toSet());

        List<Campaign> newCampaigns = mappingService.upsertCampaignNames(sid, campaignNames);

        log.info("Inserted {} rows of campaign", newCampaigns.size());

        mappingService.mappingSameNameCampaignsToOrderLinks(sid, newCampaigns);
    }
}
