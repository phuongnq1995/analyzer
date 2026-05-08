package org.phuongnq.analyzer.query.model.evaluate;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.phuongnq.analyzer.query.model.EfficiencyLevel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EfficiencyResults {
    private EfficiencyLevel efficiencyLevel;
    @JsonPropertyDescription("Vietnamese response, maximum 300 words")
    private String briefStatusSummary;
    @JsonPropertyDescription("Vietnamese response, 1 - 2 recommendation actions, maximum 500 words")
    private String[] recommendedActions;
}
