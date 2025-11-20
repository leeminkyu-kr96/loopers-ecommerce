package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Product;
import com.loopers.domain.common.Quantity;
import com.loopers.domain.common.Money;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "orderitems")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Embedded
    private Quantity quantity;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "order_price"))
    private Money orderPrice;

    protected OrderItem() {}

    private OrderItem(Product product, Quantity quantity, Money orderPrice) {
        this.product = product;
        this.quantity = quantity;
        this.orderPrice = orderPrice;
    }

    public static OrderItem create(Product product, Quantity quantity) {
        long calculatedPrice = product.getPrice().value() * quantity.quantity();
        return new OrderItem(product, quantity, new Money(calculatedPrice));
    }

    protected void setOrder(Order order) {
        this.order = order;
    }
}