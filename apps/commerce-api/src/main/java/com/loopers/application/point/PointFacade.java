package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserId;
import com.loopers.domain.common.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    /**
     * 포인트를 충전합니다.
     */
    @Transactional
    public PointInfo charge(UserId userId, Money amount) {
        Point point = pointService.charge(userId, amount);
        return PointInfo.from(point);
    }

    /**
     * 보유 포인트를 조회합니다.
     */
    @Transactional(readOnly = true)
    public PointInfo getPoint(UserId userId) {
        Point point = pointService.getPoint(userId);
        return PointInfo.from(point);
    }
}
