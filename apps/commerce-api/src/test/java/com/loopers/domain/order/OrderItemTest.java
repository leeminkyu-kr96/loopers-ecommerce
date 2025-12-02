package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderItemTest {
    @DisplayName("주문 항목 모델 생성")
    @Nested
    class Create {
        
        @DisplayName("주문 항목이 정상적으로 생성된다")
        @Test
        void createsOrderItem_whenValidParameters() {
            // arrange
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            Quantity quantity = new Quantity(3);
            Money orderPrice = new Money(30000);

            // act
            OrderItem orderItem = OrderItem.create(product, quantity);

            // assert
            assertAll(
                () -> assertThat(orderItem).isNotNull(),
                () -> assertThat(orderItem.getProduct()).isEqualTo(product),
                () -> assertThat(orderItem.getQuantity()).isEqualTo(quantity),
                () -> assertThat(orderItem.getOrderPrice()).isEqualTo(orderPrice)
            );
        }

        @DisplayName("주문 항목의 가격이 상품 가격과 수량의 곱과 일치한다")
        @Test
        void createsOrderItem_withCorrectPriceCalculation() {
            // arrange
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            Quantity quantity = new Quantity(2);
            Money expectedOrderPrice = new Money(20000); // 10000 * 2

            // act
            OrderItem orderItem = OrderItem.create(product, quantity);

            // assert
            assertThat(orderItem.getOrderPrice().value()).isEqualTo(expectedOrderPrice.value());
        }

        @DisplayName("수량이 1일 때 가격이 상품 가격과 일치한다")
        @Test
        void createsOrderItem_whenQuantityIsOne() {
            // arrange
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            Quantity quantity = new Quantity(1);

            // act
            OrderItem orderItem = OrderItem.create(product, quantity);

            // assert
            assertThat(orderItem.getOrderPrice().value()).isEqualTo(product.getPrice().value());
        }

        @DisplayName("상품 가격이 0원일 때도 정상 동작한다")
        @Test
        void createsOrderItem_whenProductPriceIsZero() {
            // arrange
            Product product = new Product("product1", new Brand("Apple"), new Money(0), new Quantity(10), 0L);
            Quantity quantity = new Quantity(5);
            Money expectedOrderPrice = new Money(0);

            // act
            OrderItem orderItem = OrderItem.create(product, quantity);

            // assert
            assertThat(orderItem.getOrderPrice()).isEqualTo(expectedOrderPrice);
        }

        @DisplayName("수량이 여러 개일 때 가격이 정확히 계산된다")
        @Test
        void createsOrderItem_withMultipleQuantities() {
            // arrange
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            Quantity quantity = new Quantity(5);
            Money expectedOrderPrice = new Money(50000); // 10000 * 5

            // act
            OrderItem orderItem = OrderItem.create(product, quantity);

            // assert
            assertThat(orderItem.getOrderPrice()).isEqualTo(expectedOrderPrice);
        }
    }

    @DisplayName("OrderItem 검증 테스트")
    @Nested
    class Validation {
        
        @DisplayName("수량이 0이면 정상 생성된다")
        @Test
        void createsQuantity_whenZero() {
            // arrange & act
            Quantity quantity = new Quantity(0);

            // assert
            assertThat(quantity.quantity()).isEqualTo(0);
        }

        @DisplayName("수량이 음수이면 예외가 발생한다")
        @Test
        void throwsException_whenQuantityIsNegative() {
            // arrange
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                new Quantity(-1);
            });
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("setOrder() 테스트")
    @Nested
    class SetOrder {
        
        @DisplayName("Order가 정상적으로 설정된다")
        @Test
        void setsOrder_whenValidOrder() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3));
            List<OrderItem> orderItems = List.of(orderItem);
            Order order = Order.create(user, orderItems);

            // act & assert
            assertThat(orderItem.getOrder()).isEqualTo(order);
        }
    }
}

