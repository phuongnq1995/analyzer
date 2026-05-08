package org.phuongnq.analyzer.query.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EfficiencyLevel {
    VERY_EFFICIENT(5), EFFICIENT(4), OK(3), BAD(2), VERY_BAD(1);

    private final int value;
}
