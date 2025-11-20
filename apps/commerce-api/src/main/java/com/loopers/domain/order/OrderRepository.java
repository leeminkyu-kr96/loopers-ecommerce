package com.loopers.domain.order;

import com.loopers.domain.user.User;
import java.util.Optional;
import java.util.List;

public interface OrderRepository {
    // 주문 저장
    Order save(Order order);
    // 주문 단건 조회
    Optional<Order> findById(Long id);
    // 사용자 주문 조회
    List<Order> findByUserId(User user);
}

