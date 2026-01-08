package org.phuongnq.analyzer.query.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DelayPeriod {
    SAME_DAY,
    ONE_DAY,
    TWO_DAYS,
    MORE_THAN_TWO_DAYS;

    public static DelayPeriod fromDelay(int delay) {
        return switch (delay) {
            case 0 -> SAME_DAY;
            case 1 -> ONE_DAY;
            case 2 -> TWO_DAYS;
            default -> MORE_THAN_TWO_DAYS;
        };
    }
}
