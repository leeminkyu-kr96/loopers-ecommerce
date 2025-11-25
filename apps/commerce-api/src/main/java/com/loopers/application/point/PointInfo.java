package com.loopers.application.point;

import com.loopers.domain.common.Money;
import com.loopers.domain.point.Point;

public record PointInfo(Long id, String userId, Money balance) {
    public static PointInfo from(Point point) {
        return new PointInfo(
            point.getId(),
            point.getUser().getUserId().userId(),
            point.getBalance()
        );
    }
}
