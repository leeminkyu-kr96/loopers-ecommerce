package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ProductServiceIntegrationTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 조회")
    @Nested
    class Get {
        @DisplayName("상품 다건 조회 시 결과가 반환된다.")
        @Test
        void getProducts_returnsProducts() {
            // arrange
            productJpaRepository.save(new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L));
            productJpaRepository.save(new Product("product2", new Brand("Samsung"), new Money(20000), new Quantity(20), 0L));

            // act
            Page<Product> result = productService.getProducts(Pageable.ofSize(10), "latest", null);

            // assert
            assertThat(result.getContent()).hasSize(2);
        }
        
        @DisplayName("상품 단건 조회 시 상품이 존재하면 반환된다.")
        @Test
        void getProduct_returnsProduct() {
            // arrange
            Product product = productJpaRepository.save(new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L));

            // act
            Product result = productService.getProduct(product.getId());

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(product.getId());
        }

        @DisplayName("상품 단건 조회 시 상품이 없으면 NOT_FOUND 예외가 발생한다.")
        @Test
        void getProduct_throwsException_whenNotFound() {
            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> productService.getProduct(999L));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
    
    @DisplayName("재고 수정")
    @Nested
    class UpdateQuantity {
        @DisplayName("재고 차감 성공")
        @Test
        void updateQuantity_decreasesStock() {
            // arrange
            Product product = productJpaRepository.save(new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L));

            // act
            productService.updateQuantity(product.getId(), new Quantity(3));

            // assert
            Product updatedProduct = productJpaRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getQuantity().quantity()).isEqualTo(7);
        }
        
        @DisplayName("재고 부족 시 예외 발생")
        @Test
        void updateQuantity_throwsException_whenInsufficientStock() {
            // arrange
            Product product = productJpaRepository.save(new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(10), 0L));

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> productService.updateQuantity(product.getId(), new Quantity(11)));
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("동시성 테스트: 동시에 100명이 1개씩 주문하면 재고가 정확히 차감되어야 한다.")
        @Test
        void updateQuantity_concurrency() throws InterruptedException {
            // arrange
            int initialStock = 100;
            Product product = productJpaRepository.save(new Product("product1", new Brand("Apple"), new Money(10000), new Quantity(initialStock), 0L));
            
            int numberOfThreads = 100;
            ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            // act
            for (int i = 0; i < numberOfThreads; i++) {
                executorService.submit(() -> {
                    try {
                        productService.updateQuantity(product.getId(), new Quantity(1));
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();

            // assert
            Product updatedProduct = productJpaRepository.findById(product.getId()).orElseThrow();
            assertThat(updatedProduct.getQuantity().quantity()).isEqualTo(0);
        }
    }
}
