package org.phuongnq.analyzer.service;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.phuongnq.analyzer.dto.req.DateRange;
import org.phuongnq.analyzer.query.AffQuery;
import org.phuongnq.analyzer.query.BatchOperation;
import org.phuongnq.analyzer.query.model.ConversionPacingCurve;
import org.phuongnq.analyzer.query.model.OrderDelay;
import org.phuongnq.analyzer.utils.MathUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AggregationBatchService {

    private final AffQuery query;
    private final BatchOperation batchOperation;

    @Async
    @Transactional(propagation = REQUIRES_NEW)
    public void aggregateAfterIngestOrders(Long sid, DateRange input) {
        LocalDate from = input.getFromDate();;
        LocalDate to = input.getToDate();

        batchOperation.deleteConversionPacingCurve(sid, from, to);

        List<ConversionPacingCurve> curves = new ArrayList<>();

        Map<Pair<String, LocalDate>, List<OrderDelay>> delayOrderMap = query.getDistributeOrder(sid, from, to)
            .stream()
            .collect(Collectors.groupingBy(orderDelay -> Pair.of(orderDelay.getName(), orderDelay.getClickDate())));

        for (Entry<Pair<String, LocalDate>, List<OrderDelay>> entry : delayOrderMap.entrySet()) {
            BigDecimal totalRevenue = entry.getValue().stream()
                .map(OrderDelay::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            entry.getValue().forEach(orderDelay ->
                curves.add(ConversionPacingCurve.builder()
                    .sId(sid)
                    .name(entry.getKey().getLeft())
                    .date(entry.getKey().getRight())
                    .delayDate(orderDelay.getDelayDays())
                    .revenue(orderDelay.getRevenue())
                    .percentage(MathUtils.toPercentageOf(orderDelay.getRevenue(), totalRevenue))
                    .build()));
        }

        batchOperation.batchInsert(curves);
    }
}
