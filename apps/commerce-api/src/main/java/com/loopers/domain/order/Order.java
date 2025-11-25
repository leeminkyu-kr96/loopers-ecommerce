package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList;

@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_price"))
    private Money totalPrice;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order", orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    protected Order() {}

    private Order(User user, List<OrderItem> orderItems) {
        this.user = user;
        for (OrderItem item : orderItems) {
            this.addOrderItem(item);
        }
        this.totalPrice = calculateTotalPrice();
    }

    public static Order create(User user, List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 필수입니다.");
        }
        return new Order(user, orderItems);
    }

    public static Order createWithPrice(User user, List<OrderItem> orderItems, Money finalPrice) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 필수입니다.");
        }
        Order order = new Order(user, orderItems);
        order.totalPrice = finalPrice;
        return order;
    }

    private void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    private Money calculateTotalPrice() {
        long total = this.orderItems.stream()
                .mapToLong(item -> item.getOrderPrice().value())
                .sum();
        return new Money(total);
    }
}