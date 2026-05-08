package org.phuongnq.analyzer.repository.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.stream.Stream;
import org.phuongnq.analyzer.query.model.EfficiencyLevel;

@Converter(autoApply = true) // autoApply makes this converter the default for all Status fields
public class EvaluateLevelAttributeConverter implements AttributeConverter<EfficiencyLevel, String> {

    @Override
    public String convertToDatabaseColumn(EfficiencyLevel value) {
        if (value == null) {
            return null;
        }
        return value.name();
    }

    @Override
    public EfficiencyLevel convertToEntityAttribute(String dbValue) {
        if (dbValue == null) {
            return null;
        }

        return Stream.of(EfficiencyLevel.values())
            .filter(s -> s.name().equals(dbValue))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
