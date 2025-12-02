package com.loopers.domain.order;

import com.loopers.domain.point.Point;
import com.loopers.domain.product.Product;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OrderServiceIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("주문 생성")
    @Nested
    class CreateOrder {
        
        @DisplayName("정상 주문이 성공적으로 생성된다")
        @Test
        void createsOrder_whenValidOrderRequest() {
            // arrange
            User user = userJpaRepository.save(
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(50000));
            pointJpaRepository.save(point);
            Product product1 = productJpaRepository.save(
                new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L)
            );
            Product product2 = productJpaRepository.save(
                new Product("product2", new Brand("Samsung"), new Money(20000), new Quantity(5), 0L)
            );

            List<OrderItem> items = List.of(
                OrderItem.create(product1, new Quantity(2)),
                OrderItem.create(product2, new Quantity(1))
            );
            Money totalPrice = new Money(40000); // 10000*2 + 20000*1

            // act
            Order order = orderService.createOrder(user, items, totalPrice);

            // assert
            assertAll(
                () -> assertThat(order).isNotNull(),
                () -> assertThat(order.getUser()).isEqualTo(user),
                () -> assertThat(order.getTotalPrice().value()).isEqualTo(40000), // 10000*2 + 20000*1
                () -> assertThat(order.getOrderItems()).hasSize(2),
                () -> assertThat(order.getOrderItems().get(0).getProduct().getId()).isEqualTo(product1.getId()),
                () -> assertThat(order.getOrderItems().get(0).getQuantity().quantity()).isEqualTo(2),
                () -> assertThat(order.getOrderItems().get(1).getProduct().getId()).isEqualTo(product2.getId()),
                () -> assertThat(order.getOrderItems().get(1).getQuantity().quantity()).isEqualTo(1)
            );
        }

        @DisplayName("재고 부족 시 주문 생성이 실패한다")
        @Test
        void throwsException_whenInsufficientStock() {
            // arrange
            User user = userJpaRepository.save(
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(50000));
            pointJpaRepository.save(point);
            Product product = productJpaRepository.save(
                new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(5), 0L)
            );

            List<OrderItem> items = List.of(
                OrderItem.create(product, new Quantity(10)) // 재고보다 많은 수량
            );
            Money totalPrice = new Money(100000); // 10000 * 10

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                orderService.createOrder(user, items, totalPrice);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("재고가 부족합니다");
        }

        @DisplayName("포인트 부족 시 주문 생성이 실패한다")
        @Test
        void throwsException_whenInsufficientPoints() {
            // arrange
            User user = userJpaRepository.save(
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(10000)); // 부족한 포인트
            pointJpaRepository.save(point);
            Product product = productJpaRepository.save(
                new Product("product1", new Brand("Apple"), new Money(20000), new Quantity(10), 0L)
            );
            List<OrderItem> items = List.of(OrderItem.create(product, new Quantity(1)));
            Money totalPrice = new Money(20000);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                orderService.createOrder(user, items, totalPrice);
            });
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("포인트가 부족합니다");
        }

        @DisplayName("존재하지 않는 상품으로 주문 시 실패한다")
        @Test
        void throwsException_whenProductNotFound() {
            // arrange
            User user = userJpaRepository.save(
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(50000));
            pointJpaRepository.save(point);

            // arrange - 존재하지 않는 상품으로 OrderItem 생성
            Product nonExistentProduct = new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            List<OrderItem> items = List.of(OrderItem.create(nonExistentProduct, new Quantity(1)));
            Money totalPrice = new Money(10000);

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                orderService.createOrder(user, items, totalPrice);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
            assertThat(exception.getMessage()).contains("상품을 찾을 수 없습니다");
        }

        @DisplayName("주문 생성 시 재고가 정확히 차감된다")
        @Test
        void decreasesStock_whenOrderIsCreated() {
            // arrange
            User user = userJpaRepository.save(
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(50000));
            pointJpaRepository.save(point);
            Product product = productJpaRepository.save(
                new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L)
            );
            int initialQuantity = product.getQuantity().quantity();
            List<OrderItem> items = List.of(OrderItem.create(product, new Quantity(3)));
            Money totalPrice = new Money(30000); // 10000 * 3

            // act
            orderService.createOrder(user, items, totalPrice);

            // assert
            Product updatedProduct = productJpaRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getQuantity().quantity()).isEqualTo(initialQuantity - 3);
        }

        @DisplayName("주문 생성 시 포인트가 정확히 차감된다")
        @Test
        void decreasesPoints_whenOrderIsCreated() {
            // arrange
            User user = userJpaRepository.save(
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(50000));
            pointJpaRepository.save(point);

            Product product = productJpaRepository.save(
                new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L)
            );
            long initialPoints = point.getBalance().value();
            List<OrderItem> items = List.of(OrderItem.create(product, new Quantity(2)));
            Money totalPrice = new Money(20000); // 10000 * 2

            // act
            orderService.createOrder(user, items, totalPrice);

            // assert
            Point updatedPoint = pointJpaRepository.findByUser(user).orElseThrow();
            assertThat(updatedPoint.getBalance().value()).isEqualTo(initialPoints - 20000);
        }
    }
}

