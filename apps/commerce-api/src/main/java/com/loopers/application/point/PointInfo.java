package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;

public record PointInfo(Long id, Long userId, Money balance) {
    public static PointInfo from(Point point) {
        return new PointInfo(
            point.getId(),
            point.getUser().getId(),
            point.getBalance()
        );
    }
}