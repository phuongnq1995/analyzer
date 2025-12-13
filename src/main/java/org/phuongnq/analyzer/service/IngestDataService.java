package org.phuongnq.analyzer.service;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.phuongnq.analyzer.dto.aff.AdsDto;
import org.phuongnq.analyzer.dto.aff.OrderDto;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.BatchOperation;
import org.phuongnq.analyzer.query.model.AggregationByDateResult;
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
    private final UserService service;
    private final AggregationStatisticService statisticService;

    @Transactional
    public void ingestOrders(MultipartFile file, DateRange input) {
        Long sid = service.getCurrentShopId();
        int count = affQuery.cleanOrdersData(sid, input);
        List<OrderDto> orders = csvHelper.readOrderFromCsv(file);
        log.info("Deleted {} rows of orders from {} to {}", count, input.getFromDate(), input.getToDate());
        batchOperation.batchInsertOrUpdateOrders(sid, orders);
        List<AggregationByDateResult> statistics = statisticService.getCompareAggregationStatistics(
            input.getFromDate(), input.getToDate());

    }

    @Transactional
    public void ingestAds(MultipartFile file, DateRange input) {
        Long sid = service.getCurrentShopId();
        List<AdsDto> ads = csvHelper.readAdFromCsv(file);
        int count = affQuery.cleanAdsData(sid, input);
        log.info("Deleted {} rows of ads from {} to {}", count, input.getFromDate(), input.getToDate());

        batchOperation.batchInsertOrUpdateAds(sid, ads);
    }

    private String sumBigDecimal(String bd1, String bd2) {
        return new BigDecimal(bd1).add(new BigDecimal(bd2)).toString();
    }
}
