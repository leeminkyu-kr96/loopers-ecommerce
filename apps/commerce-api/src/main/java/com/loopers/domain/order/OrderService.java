package com.loopers.domain.order;

import com.loopers.application.order.OrderDto;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.point.PointService;
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

    @Transactional
    public Order createOrder(User user, List<OrderDto.CreateRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목이 비어있습니다.");
        }

        List<OrderItem> orderItems = requests.stream()
                .map(this::createOrderItemWithoutStockDecrease)
                .collect(Collectors.toList());

        Order order = Order.create(user, orderItems);

        pointService.use(user, order.getTotalPrice());

        orderItems.forEach(item -> 
            productService.getProductWithLockAndDecreaseQuantity(
                item.getProduct().getId(), 
                item.getQuantity()
            )
        );

        return orderRepository.save(order);
    }

    private OrderItem createOrderItemWithoutStockDecrease(OrderDto.CreateRequest request) {
        // 재고 차감 없이 Product만 조회하여 OrderItem 생성
        Product product = productService.getProduct(request.productId());
        if (product == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다. productId: " + request.productId());
        }
        return OrderItem.create(product, new Quantity(request.quantity()));
    }
}