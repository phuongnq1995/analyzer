package org.phuongnq.analyzer.utils;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.phuongnq.analyzer.dto.aff.AdsDto;
import org.phuongnq.analyzer.dto.aff.OrderDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Component
public class CSVHelper {

    public static String TYPE = "text/csv";

    public static final Map<String, String> FIELD_MAP = new HashMap<>() {{
        put("Tên chiến dịch", "campaignName");
        put("Tên nhóm quảng cáo", "adGroupName");
        put("Ngày", "date");
        put("Tên quảng cáo", "adName");
        put("Trạng thái phân phối", "deliveryStatus");
        put("Cấp độ phân phối", "deliveryLevel");
        put("Người tiếp cận", "reach");
        put("Lượt hiển thị", "impressions");
        put("Tần suất", "frequency");
        put("Cài đặt ghi nhận", "attributionSetting");
        put("Loại kết quả", "resultType");
        put("Kết quả", "results");
        put("Số tiền đã chi tiêu (VND)", "amountSpent");
    }};

    public CSVHelper() {
        Map<String, String> updatedMap = FIELD_MAP.entrySet().stream()
            .collect(Collectors.toMap(
                e -> "\ufeff" + e.getKey(),
                Map.Entry::getValue,
                (oldValue, newValue) -> oldValue
            ));

        FIELD_MAP.putAll(updatedMap);
    }

    public List<OrderDto> readOrderFromCsv(MultipartFile file) {
        // Check file type
        if (!TYPE.equals(file.getContentType())) {
            throw new RuntimeException("File type is not CSV! Found: " + file.getContentType());
        }

        ColumnPositionMappingStrategy<OrderDto> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(OrderDto.class);
        strategy.setColumnMapping(OrderDto.FIELDS);

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            List<OrderDto> rows = new CsvToBeanBuilder<OrderDto>(reader)
                .withSkipLines(1)
                .withMappingStrategy(strategy)
                .build()
                .parse();

            return rows;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public List<AdsDto> readAdFromCsv(MultipartFile file) {
        if (!TYPE.equals(file.getContentType())) {
            throw new RuntimeException("File type is not CSV! Found: " + file.getContentType());
        }

        try (CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(file.getInputStream())))) {
            HeaderColumnNameTranslateMappingStrategy<AdsDto> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
            strategy.setType(AdsDto.class);
            strategy.setColumnMapping(FIELD_MAP);

            return new CsvToBeanBuilder<AdsDto>(reader)
                .withMappingStrategy(strategy)
                .withIgnoreLeadingWhiteSpace(true)
                .build()
                .parse();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}