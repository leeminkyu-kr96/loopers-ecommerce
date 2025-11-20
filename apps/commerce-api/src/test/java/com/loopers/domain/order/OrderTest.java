package com.loopers.domain.order;

import com.loopers.domain.user.*;
import com.loopers.domain.user.User;
import com.loopers.domain.product.Product;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class OrderTest {
    @DisplayName("주문 모델 생성")
    @Nested
    class Create {
        
        @DisplayName("주문이 정상적으로 생성된다")
        @Test
        void createsOrder_whenValidParameters() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), new Gender("male"), new BirthDate("1999-01-01"));
            Money totalPrice = new Money(30000);
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10));
            OrderItem orderItem = new OrderItem(product, new Quantity(3), new Money(30000));
            List<OrderItem> orderItems = List.of(orderItem);

            // act
            Order order = new Order(user, totalPrice, orderItems);

            // assert
            assertAll(
                () -> assertThat(order).isNotNull(),
                () -> assertThat(order.getUser()).isEqualTo(user),
                () -> assertThat(order.getTotalPrice()).isEqualTo(totalPrice),
                () -> assertThat(order.getOrderItems()).hasSize(1),
                () -> assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem)
            );
        }

        @DisplayName("여러 주문 항목이 포함된 주문이 정상적으로 생성된다")
        @Test
        void createsOrder_whenMultipleOrderItems() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), new Gender("male"), new BirthDate("1999-01-01"));
            Money totalPrice = new Money(50000);
            Product product1 = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10));
            Product product2 = new Product("product2", new Brand("Samsung"), new Money(20000), new Quantity(5));
            OrderItem orderItem1 = new OrderItem(product1, new Quantity(2), new Money(20000));
            OrderItem orderItem2 = new OrderItem(product2, new Quantity(1), new Money(20000));
            List<OrderItem> orderItems = List.of(orderItem1, orderItem2);

            // act
            Order order = new Order(user, totalPrice, orderItems);

            // assert
            assertAll(
                () -> assertThat(order).isNotNull(),
                () -> assertThat(order.getUser()).isEqualTo(user),
                () -> assertThat(order.getTotalPrice()).isEqualTo(totalPrice),
                () -> assertThat(order.getOrderItems()).hasSize(2),
                () -> assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem1),
                () -> assertThat(order.getOrderItems().get(1)).isEqualTo(orderItem2)
            );
        }
    }

    @DisplayName("주문 항목 추가")
    @Nested
    class AddOrderItem {
        
        @DisplayName("주문 항목이 정상적으로 추가된다")
        @Test
        void addsOrderItem_whenValidOrderItem() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), new Gender("male"), new BirthDate("1999-01-01"));
            Money totalPrice = new Money(10000);
            Order order = new Order(user, totalPrice, new java.util.ArrayList<>());
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10));
            OrderItem orderItem = new OrderItem(product, new Quantity(1), new Money(10000));

            // act
            order.addOrderItem(orderItem);

            // assert
            assertAll(
                () -> assertThat(order.getOrderItems()).hasSize(1),
                () -> assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem),
                () -> assertThat(orderItem.getOrder()).isEqualTo(order)
            );
        }

        @DisplayName("여러 주문 항목이 순차적으로 추가된다")
        @Test
        void addsMultipleOrderItems_whenCalledMultipleTimes() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), new Gender("male"), new BirthDate("1999-01-01"));
            Money totalPrice = new Money(30000);
            Order order = new Order(user, totalPrice, new java.util.ArrayList<>());
            Product product1 = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10));
            Product product2 = new Product("product2", new Brand("Samsung"), new Money(20000), new Quantity(5));
            OrderItem orderItem1 = new OrderItem(product1, new Quantity(1), new Money(10000));
            OrderItem orderItem2 = new OrderItem(product2, new Quantity(1), new Money(20000));

            // act
            order.addOrderItem(orderItem1);
            order.addOrderItem(orderItem2);

            // assert
            assertAll(
                () -> assertThat(order.getOrderItems()).hasSize(2),
                () -> assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem1),
                () -> assertThat(order.getOrderItems().get(1)).isEqualTo(orderItem2),
                () -> assertThat(orderItem1.getOrder()).isEqualTo(order),
                () -> assertThat(orderItem2.getOrder()).isEqualTo(order)
            );
        }
    }
}

