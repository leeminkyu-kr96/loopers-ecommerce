package com.loopers.application.order;

import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderItem;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.user.UserId;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class OrderFacade {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public OrderInfo getOrder(Long id) {
        Order order = orderService.getOrder(id);
        if (order == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다.");
        }
        return OrderInfo.from(order);
    }

    @Transactional(readOnly = true)
    public List<OrderInfo> getUserOrders(UserId userId) {
        User user = userService.getUser(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));
        
        List<Order> orders = orderService.getUserOrders(user);
        return orders.stream()
            .map(OrderInfo::from)
            .collect(Collectors.toList());
    }

    /**
     * 주문을 생성합니다.
     * 트랜잭션으로 보장되는 처리:
     * 1. 상품 재고 확인 및 차감 (락 사용)
     * 2. 포인트 차감
     * 3. 주문 생성
     * 
     * 하나라도 실패하면 모두 롤백됩니다.
     */
    @Transactional
    public OrderInfo createOrder(UserId userId, OrderDto.CreateRequest request) {
        // 1. 사용자 조회
        User user = userService.getUser(userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 요청입니다."));

        // 2. 주문 항목 생성 (재고 차감 없이)
        List<OrderItem> orderItems = request.items().stream()
                .map(item -> {
                    Product product = productService.getProduct(item.productId());
                    if (product == null) {
                        throw new CoreException(ErrorType.NOT_FOUND, 
                            "상품을 찾을 수 없습니다. productId: " + item.productId());
                    }
                    return OrderItem.create(product, new Quantity(item.quantity()));
                })
                .collect(Collectors.toList());

        // 3. 주문 금액 계산
        Money totalPrice = calculateTotalPrice(orderItems);

        // 4. 주문 생성 (재고 차감, 포인트 차감 포함)
        Order order = orderService.createOrder(user, orderItems, totalPrice);
        
        return OrderInfo.from(order);
    }

    private Money calculateTotalPrice(List<OrderItem> orderItems) {
        long total = orderItems.stream()
                .mapToLong(item -> item.getOrderPrice().value())
                .sum();
        return new Money(total);
    }
}