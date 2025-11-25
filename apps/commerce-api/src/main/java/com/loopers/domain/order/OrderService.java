package com.loopers.domain.order;

import com.loopers.domain.user.User;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final PointService pointService;

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserId(user);
    }

    /**
     * 주문을 생성합니다.
     * 재고 차감, 포인트 차감은 이 메서드에서 처리됩니다.
     * 
     * @param user 주문 사용자
     * @param orderItems 주문 항목 목록
     * @param finalPrice 최종 결제 금액
     * @return 생성된 주문
     */
    @Transactional
    public Order createOrder(User user, List<OrderItem> orderItems, Money finalPrice) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목이 비어있습니다.");
        }

        // 재고 차감 (락을 사용하여 동시성 제어)
        orderItems.forEach(item -> 
            productService.getProductWithLockAndDecreaseQuantity(
                item.getProduct().getId(), 
                item.getQuantity()
            )
        );

        // 포인트 차감
        pointService.use(user, finalPrice);

        // 주문 생성 (최종 금액으로 설정)
        Order order = Order.createWithPrice(user, orderItems, finalPrice);

        return orderRepository.save(order);
    }
}