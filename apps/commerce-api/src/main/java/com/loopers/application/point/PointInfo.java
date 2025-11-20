package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;

public record PointInfo(Long id, User user, Money point) {
    public static PointInfo from(Point model) {
        return new PointInfo(model.getId(), model.getUser(), model.getPoint());
    }
    public Money getPoint() {
        return point;
    }
}
