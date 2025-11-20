package com.loopers.application.order;

import com.loopers.domain.common.Money;
import java.util.List;
import com.loopers.domain.order.Order;

public record OrderInfo(Long id, Long userId, Money totalPrice, List<OrderItemInfo> orderItems) {
    public static OrderInfo from(Order order) {
            List<OrderItemInfo> items = order.getOrderItems().stream()
                .map(item -> new OrderItemInfo(
                    item.getProduct().getId(),
                    item.getQuantity().quantity(),
                    item.getOrderPrice()
                ))
                .toList();
            return new OrderInfo(
                order.getId(),
                order.getUser().getId(),
                order.getTotalPrice(),
                items
            );
        }
    }
