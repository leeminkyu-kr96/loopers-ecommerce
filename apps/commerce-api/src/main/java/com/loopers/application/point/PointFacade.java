package com.loopers.application.point;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.UserId;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;
    private final UserService userService;

    public PointInfo getPoint(UserId userId) {
        User user = userService.getUser(userId);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다.");
        }
        Point pointModel = new Point(user, new Money(0));
        Point point = pointService.findPoint(pointModel);
        
        if (point == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "포인트 정보가 없습니다.");
        }
        
        return PointInfo.from(point);
    }

    public PointInfo chargePoint(UserId userId, Money point) {
        User user = userService.getUser(userId);
        if (user == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다.");
        }
        Point pointModel = new Point(user, point);
        pointService.charge(pointModel);
        
        Point charged = pointService.findPoint(new Point(user, point));
        return PointInfo.from(charged);
    }
}
