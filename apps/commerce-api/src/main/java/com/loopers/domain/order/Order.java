package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import lombok.Getter;

import java.util.List;
import java.util.ArrayList;

@Getter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_price"))
    private Money totalPrice;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();

    public Order() {
    }

    public Order(User user, Money totalPrice, List<OrderItem> orderItems) {
        this.user = user;
        this.totalPrice = totalPrice;
        orderItems.forEach(this::addOrderItem);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}
