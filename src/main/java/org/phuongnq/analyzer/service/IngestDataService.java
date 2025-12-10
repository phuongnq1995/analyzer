package org.phuongnq.analyzer.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.phuongnq.analyzer.dto.AdsDto;
import org.phuongnq.analyzer.dto.AffiliateOrderDto;
import org.phuongnq.analyzer.dto.ClickDto;
import org.phuongnq.analyzer.query.BatchOperation;
import org.phuongnq.analyzer.repository.PostRepository;
import org.phuongnq.analyzer.utils.CSVHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class IngestDataService {

    private final CSVHelper csvHelper;

    private final PostRepository postRepository;
    private final BatchOperation batchOperation;

    @Transactional
    public void ingestClicks(MultipartFile file) {
        List<ClickDto> clicks = csvHelper.readClicksFromCsv(file);
        batchOperation.batchInsertOrUpdateCLicks(clicks);
    }

    @Transactional
    public void ingestOrders(MultipartFile file) {
        List<AffiliateOrderDto> orders = csvHelper.readOrderFromCsv(file);
        batchOperation.batchInsertOrUpdateOrders(orders);
    }

    @Transactional
    public void ingestAds(MultipartFile file) {
        List<AdsDto> ads = csvHelper.readAdFromCsv(file);
        batchOperation.batchInsertOrUpdateAds(ads);
    }
}
