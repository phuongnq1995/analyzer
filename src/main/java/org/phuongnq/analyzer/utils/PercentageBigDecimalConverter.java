package org.phuongnq.analyzer.utils;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import java.math.BigDecimal;

public class PercentageBigDecimalConverter extends AbstractBeanField {

    @Override
    public Object convert(String value) throws CsvDataTypeMismatchException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String cleanValue = value.trim().replace("%", ""); // Remove the '%' symbol

        try {
            // Use the BigDecimal string constructor for precision
            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            // Wrap the exception in CsvDataTypeMismatchException
            CsvDataTypeMismatchException dtm = new CsvDataTypeMismatchException(
                String.format("Conversion of %s to BigDecimal failed.", value));
            dtm.initCause(e);
            throw dtm;
        }
    }

}