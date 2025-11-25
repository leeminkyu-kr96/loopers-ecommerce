package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderInfo;
import com.loopers.application.user.UserInfo;
import com.loopers.domain.common.Money;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class OrderV1Dto {
    public record OrderResponse(Long id, Long userId, Money totalPrice, List<OrderItemResponse> orderItems) {
        public static OrderResponse from(OrderInfo info) {
            List<OrderItemResponse> items = info.orderItems().stream()
                    .map(item -> new OrderItemResponse(
                            item.productId(),
                            item.quantity(),
                            item.orderPrice()
                    ))
                    .toList();
            return new OrderResponse(
                info.id(),
                info.userId(),
                info.totalPrice(),
                items
            );
        }
    }

    public record OrderItemResponse(Long productId, int quantity, Money orderPrice) {
    }

}
