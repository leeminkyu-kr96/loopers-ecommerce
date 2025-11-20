package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final PointService pointService;

    @Transactional(readOnly = true)
    public PointInfo getPoint(UserId userId) {
        Point point = pointService.getPoint(userId);
        return PointInfo.from(point);
    }

    @Transactional
    public PointInfo chargePoint(UserId userId, PointDto.ChargeRequest request) {
        Point point = pointService.charge(userId, request.amount());
        return PointInfo.from(point);
    }
}