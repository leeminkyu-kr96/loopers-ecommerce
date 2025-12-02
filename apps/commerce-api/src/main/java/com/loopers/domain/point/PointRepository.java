package com.loopers.domain.point;

import com.loopers.domain.user.User;
import java.util.Optional;

public interface PointRepository {
    Optional<Point> findByUser(User user);
    Optional<Point> findByUserWithLock(User user);

    Point save(Point point);
}