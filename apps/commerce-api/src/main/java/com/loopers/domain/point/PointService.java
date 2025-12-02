package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Point getPoint(UserId userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "유저를 찾을 수 없습니다."));
        
        return pointRepository.findByUser(user)
                .orElse(Point.create(user));
    }
  
    @Transactional
    public Point charge(UserId userId, Money amount) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "유저를 찾을 수 없습니다."));
      
        Point point = pointRepository.findByUserWithLock(user)
                .orElseGet(() -> pointRepository.save(Point.create(user)));
    
        point.charge(amount);
        
        return point;
    }
    
    @Transactional
    public void use(User user, Money amount) {
        Point point = pointRepository.findByUserWithLock(user)
            .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "포인트 계좌가 존재하지 않습니다."));
        
        point.use(amount);
    }
}