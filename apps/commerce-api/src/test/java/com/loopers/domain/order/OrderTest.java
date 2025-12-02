package com.loopers.domain.order;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import com.loopers.domain.product.Product;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
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

class OrderTest {
    @DisplayName("주문 모델 생성")
    @Nested
    class Create {
        
        @DisplayName("주문이 정상적으로 생성된다")
        @Test
        void createsOrder_whenValidParameters() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3));
            List<OrderItem> orderItems = List.of(orderItem);
            Money expectedTotalPrice = new Money(30000);

            // act
            Order order = Order.create(user, orderItems);

            // assert
            assertAll(
                () -> assertThat(order).isNotNull(),
                () -> assertThat(order.getUser()).isEqualTo(user),
                () -> assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice),
                () -> assertThat(order.getOrderItems()).hasSize(1),
                () -> assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem)
            );
        }

        @DisplayName("여러 주문 항목이 포함된 주문이 정상적으로 생성된다")
        @Test
        void createsOrder_whenMultipleOrderItems() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product1 = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            Product product2 = new Product("product2", new Brand("Samsung"), new Money(20000), new Quantity(5), 0L);
            OrderItem orderItem1 = OrderItem.create(product1, new Quantity(2));
            OrderItem orderItem2 = OrderItem.create(product2, new Quantity(1));
            List<OrderItem> orderItems = List.of(orderItem1, orderItem2);
            Money expectedTotalPrice = new Money(40000); // 10000 * 2 + 20000 * 1

            // act
            Order order = Order.create(user, orderItems);

            // assert
            assertAll(
                () -> assertThat(order).isNotNull(),
                () -> assertThat(order.getUser()).isEqualTo(user),
                () -> assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice),
                () -> assertThat(order.getOrderItems()).hasSize(2),
                () -> assertThat(order.getOrderItems().get(0)).isEqualTo(orderItem1),
                () -> assertThat(order.getOrderItems().get(1)).isEqualTo(orderItem2)
            );
        }
    }

    @DisplayName("주문 생성 검증")
    @Nested
    class Validation {
        
        @DisplayName("주문 항목이 비어있으면 예외가 발생한다")
        @Test
        void throwsException_whenOrderItemsIsEmpty() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            List<OrderItem> emptyOrderItems = new java.util.ArrayList<>();

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> Order.create(user, emptyOrderItems));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("주문 항목은 필수입니다");
        }

        @DisplayName("주문 항목이 null이면 예외가 발생한다")
        @Test
        void throwsException_whenOrderItemsIsNull() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> Order.create(user, null));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("주문 항목은 필수입니다");
        }

        @DisplayName("주문 항목이 여러 개일 때 총 금액이 정확히 계산된다")
        @Test
        void calculatesTotalPrice_whenMultipleOrderItems() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product1 = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            Product product2 = new Product("product2", new Brand("Samsung"), new Money(20000), new Quantity(5), 0L);
            OrderItem orderItem1 = OrderItem.create(product1, new Quantity(2)); // 20000
            OrderItem orderItem2 = OrderItem.create(product2, new Quantity(3)); // 60000
            List<OrderItem> orderItems = List.of(orderItem1, orderItem2);
            Money expectedTotalPrice = new Money(80000); // 20000 + 60000

            // act
            Order order = Order.create(user, orderItems);

            // assert
            assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice);
        }

        @DisplayName("주문 항목이 Order와 양방향 관계로 연결된다")
        @Test
        void linksOrderItems_bidirectionally() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3));
            List<OrderItem> orderItems = List.of(orderItem);

            // act
            Order order = Order.create(user, orderItems);

            // assert
            assertAll(
                () -> assertThat(order.getOrderItems()).contains(orderItem),
                () -> assertThat(orderItem.getOrder()).isEqualTo(order)
            );
        }
    }

    @DisplayName("Order.createWithPrice() 테스트")
    @Nested
    class CreateWithPrice {
        
        @DisplayName("finalPrice가 정상적으로 설정된다")
        @Test
        void createsOrderWithPrice_whenValidPrice() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3));
            List<OrderItem> orderItems = List.of(orderItem);
            Money finalPrice = new Money(25000); // 할인된 가격

            // act
            Order order = Order.createWithPrice(user, orderItems, finalPrice);

            // assert
            assertAll(
                () -> assertThat(order.getTotalPrice()).isEqualTo(finalPrice),
                () -> assertThat(order.getUser()).isEqualTo(user),
                () -> assertThat(order.getOrderItems()).hasSize(1)
            );
        }

        @DisplayName("finalPrice가 주문 항목 합계와 다를 때도 설정된다")
        @Test
        void createsOrderWithPrice_whenPriceDiffersFromItemsTotal() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3)); // 원래 가격: 30000
            List<OrderItem> orderItems = List.of(orderItem);
            Money discountedPrice = new Money(27000); // 할인된 가격

            // act
            Order order = Order.createWithPrice(user, orderItems, discountedPrice);

            // assert
            assertThat(order.getTotalPrice()).isEqualTo(discountedPrice);
        }

        @DisplayName("finalPrice가 null이면 예외가 발생한다")
        @Test
        void throwsException_whenFinalPriceIsNull() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3));
            List<OrderItem> orderItems = List.of(orderItem);

            // act & assert
            assertThrows(NullPointerException.class, () -> {
                Order.createWithPrice(user, orderItems, null);
            });
        }

        @DisplayName("finalPrice가 음수이면 예외가 발생한다")
        @Test
        void throwsException_whenFinalPriceIsNegative() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(3));
            List<OrderItem> orderItems = List.of(orderItem);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                Order.createWithPrice(user, orderItems, new Money(-1));
            });
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("경계값 테스트")
    @Nested
    class BoundaryTests {
        
        @DisplayName("주문 항목의 가격이 0원일 때도 정상 동작한다")
        @Test
        void createsOrder_whenOrderItemPriceIsZero() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product1", new Brand("Apple"), new Money(0), new Quantity(10), 0L);
            OrderItem orderItem = OrderItem.create(product, new Quantity(1));
            List<OrderItem> orderItems = List.of(orderItem);
            Money expectedTotalPrice = new Money(0);

            // act
            Order order = Order.create(user, orderItems);

            // assert
            assertThat(order.getTotalPrice()).isEqualTo(expectedTotalPrice);
        }
    }
}

