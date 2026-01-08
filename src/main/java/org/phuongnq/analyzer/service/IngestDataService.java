package org.phuongnq.analyzer.service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
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
import org.phuongnq.analyzer.repository.UserImportRepository;
import org.phuongnq.analyzer.repository.entity.Campaign;
import org.phuongnq.analyzer.repository.entity.OrderLink;
import org.phuongnq.analyzer.repository.entity.Shop;
import org.phuongnq.analyzer.repository.entity.UserImport;
import org.phuongnq.analyzer.service.recommendation.RecommendationService;
import org.phuongnq.analyzer.utils.CSVHelper;
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
    private final MappingService mappingService;
    private final UserService service;
    private final AggregationStatisticService statisticService;
    private final ConversionCurveService conversionCurveService;
    private final UserImportRepository userImportRepository;
    private final RecommendationService recommendationService;

    @Transactional
    public void ingestOrders(MultipartFile file, DateRange input) {
        Instant start = Instant.now();
        Shop shop = service.getCurrentShop();
        Long sid = shop.getId();
        int count = affQuery.cleanOrdersData(sid, input);
        List<OrderDto> orders = csvHelper.readOrderFromCsv(file);

        log.info("Deleted {} rows of orders from {} to {}", count, input.getFromDate(), input.getToDate());

        int insertCount = batchOperation.batchInsertOrUpdateOrders(sid, orders);

        affQuery.refreshOrderData();

        log.info("Shop: {}, inserted {} rows of orders from {} to {}, in {} ms",
            sid, insertCount, input.getFromDate(), input.getToDate(), Duration.between(start, Instant.now()).toMillis());

        Set<String> subIds = orders.stream()
            .map(OrderDto::getSubId1)
            .collect(Collectors.toSet());

        List<OrderLink> orderLinks = mappingService.upsertOrderSubIds(sid, subIds);

        mappingService.mappingSameOrderSubIdsToCampaigns(sid, orderLinks);

        statisticService.cacheAggregates(shop, input);

        conversionCurveService.ingestConversionCurves(shop);

        userImportRepository.save(UserImport.builder()
            .name("orders")
            .shop(shop)
            .dataDate(Timestamp.valueOf(orders.getFirst().getOrderTime()).toLocalDateTime().toLocalDate())
            .createdTime(Instant.now())
            .build());

        recommendationService.checkAndStartEvaluate(shop);
    }

    @Transactional
    public void ingestAds(MultipartFile file, DateRange input) {
        Instant start = Instant.now();
        Shop shop = service.getCurrentShop();
        Long sid = shop.getId();
        List<AdsDto> ads = csvHelper.readAdFromCsv(file);
        int count = affQuery.cleanAdsData(sid, input);
        log.info("Shop: {}, deleted {} rows of ads from {} to {}", sid, count, input.getFromDate(), input.getToDate());

        int insertCount = batchOperation.batchInsertOrUpdateAds(sid, ads);

        affQuery.refreshAdsData();

        log.info("Shop: {}, inserted {} rows of ads from {} to {}, in {} ms",
            sid, insertCount, input.getFromDate(), input.getToDate(), Duration.between(start, Instant.now()).toMillis());

        Set<String> campaignNames = ads.stream()
            .map(AdsDto::getCampaignName)
            .collect(Collectors.toSet());

        List<Campaign> campaigns = mappingService.upsertCampaignNames(sid, campaignNames);

        mappingService.mappingSameNameCampaignsToOrderLinks(sid, campaigns);

        statisticService.cacheAggregates(shop, input);

        userImportRepository.save(UserImport.builder()
            .name("ads")
            .shop(shop)
            .dataDate(LocalDate.parse(ads.getFirst().getDate()))
            .createdTime(Instant.now())
            .build());
    }
}
