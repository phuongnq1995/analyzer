package org.phuongnq.analyzer.utils;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.phuongnq.analyzer.dto.AdsDto;
import org.phuongnq.analyzer.dto.AffiliateOrderDto;
import org.phuongnq.analyzer.dto.ClickDto;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Component
public class CSVHelper {

    public static String TYPE = "text/csv";

    public List<ClickDto> readClicksFromCsv(MultipartFile file) {
        // Check file type
        if (!TYPE.equals(file.getContentType())) {
            throw new RuntimeException("File type is not CSV! Found: " + file.getContentType());
        }

        ColumnPositionMappingStrategy<ClickDto> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(ClickDto.class);
        String[] columns = new String[]{"id", "clickTime", "areaZone", "subIds", "channel"};
        strategy.setColumnMapping(columns);

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            CsvToBean<ClickDto> csvToBean = new CsvToBeanBuilder(reader)
                .withSkipLines(1)
                .withMappingStrategy(strategy)
                .build();

            List<ClickDto> products = csvToBean.parse();

            return products;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }

    public List<AffiliateOrderDto> readOrderFromCsv(MultipartFile file) {
        // Check file type
        if (!TYPE.equals(file.getContentType())) {
            throw new RuntimeException("File type is not CSV! Found: " + file.getContentType());
        }

        ColumnPositionMappingStrategy<AffiliateOrderDto> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(AffiliateOrderDto.class);
        strategy.setColumnMapping(AffiliateOrderDto.FIELDS);

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            List<AffiliateOrderDto> rows = new CsvToBeanBuilder(reader)
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

        ColumnPositionMappingStrategy<AdsDto> strategy = new ColumnPositionMappingStrategy<>();
        strategy.setType(AdsDto.class);
        strategy.setColumnMapping(AdsDto.FIELDS);

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            List<AdsDto> rows = new CsvToBeanBuilder(reader)
                .withSkipLines(1)
                .withMappingStrategy(strategy)
                .build()
                .parse();

            return rows;

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }
    }
}