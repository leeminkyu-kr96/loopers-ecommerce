package com.loopers.domain.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.common.Quantity;
import com.loopers.domain.common.Money;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {
    @DisplayName("상품 모델을 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("상품 재고는 0 이상이어야 한다.")
        @Test
        void productModel_whenCreateQuantityIsLessThan0() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Quantity(-1);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 재고를 차감할 때, 재고가 부족하면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void productModel_whenDecreaseQuantityIsLessThan0() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                product.decreaseQuantity(new Quantity(11));
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("재고가 부족합니다");
        }

        @DisplayName("상품 재고를 정상적으로 차감한다")
        @Test
        void decreasesQuantity_whenValidQuantityIsProvided() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            int initialQuantity = product.getQuantity().quantity();
            
            // act
            product.decreaseQuantity(new Quantity(3));

            // assert
            assertThat(product.getQuantity().quantity()).isEqualTo(initialQuantity - 3);
        }

        @DisplayName("상품 재고를 0까지 차감할 수 있다")
        @Test
        void decreasesQuantityToZero_whenQuantityEqualsStock() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            
            // act
            product.decreaseQuantity(new Quantity(10));

            // assert
            assertThat(product.getQuantity().quantity()).isEqualTo(0);
        }

        @DisplayName("상품 등록 시 브랜드가 빈칸이면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void productModel_whenCreateBrandIsBlank() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Brand("");
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 등록 시 이름이 빈칸이면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void productModel_whenCreateNameIsBlank() {
            // arrange
            String name = "";
            
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(name, new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품 이름이 null이면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void productModel_whenCreateNameIsNull() {
            // arrange
            String name = null;
            
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(name, new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("상품 이름은 비어있을 수 없습니다");
        }

        @DisplayName("상품 이름이 공백 문자열이면 BAD_REQUEST 예외가 발생한다.")
        @Test
        void productModel_whenCreateNameIsWhitespace() {
            // arrange
            String name = "   ";
            
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Product(name, new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품이 정상적으로 생성된다")
        @Test
        void createsProduct_whenAllFieldsAreValid() {
            // arrange
            String name = "iPhone 15";
            Brand brand = new Brand("Apple");
            Money price = new Money(1000000);
            Quantity quantity = new Quantity(100);
            Long likeCount = 0L;
            
            // act
            Product product = new Product(name, brand, price, quantity, likeCount);
            
            // assert
            assertAll(
                () -> assertThat(product.getName()).isEqualTo(name),
                () -> assertThat(product.getBrand()).isEqualTo(brand),
                () -> assertThat(product.getPrice()).isEqualTo(price),
                () -> assertThat(product.getQuantity()).isEqualTo(quantity),
                () -> assertThat(product.getTotalLikeCount()).isEqualTo(likeCount)
            );
        }

        @DisplayName("상품 가격이 음수이면 예외가 발생한다")
        @Test
        void productModel_whenPriceIsNegative() {
            // arrange & act
            CoreException result = assertThrows(CoreException.class, () -> {
                new Money(-1);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("가격은 0 이상이어야 합니다");
        }
    }

    @DisplayName("상품 재고 차감")
    @Nested
    class DecreaseQuantity {
        
        @DisplayName("재고가 정확히 일치할 때 차감이 성공한다")
        @Test
        void decreasesQuantity_whenQuantityEqualsStock() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            
            // act
            product.decreaseQuantity(new Quantity(10));
            
            // assert
            assertThat(product.getQuantity().quantity()).isEqualTo(0);
        }

        @DisplayName("차감할 수량이 음수이면 예외가 발생한다")
        @Test
        void throwsException_whenDecreaseQuantityIsNegative() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            
            // act & assert
            CoreException result = assertThrows(CoreException.class, () -> {
                new Quantity(-1);
            });
            
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("여러 번 차감했을 때 재고가 정확히 감소한다")
        @Test
        void decreasesQuantity_multipleTimes() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            
            // act
            product.decreaseQuantity(new Quantity(3));
            product.decreaseQuantity(new Quantity(2));
            product.decreaseQuantity(new Quantity(1));
            
            // assert
            assertThat(product.getQuantity().quantity()).isEqualTo(4);
        }

        @DisplayName("재고가 0인 상품에 대해 차감 시도 시 예외가 발생한다")
        @Test
        void throwsException_whenStockIsZero() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(0), 0L);
            
            // act & assert
            CoreException result = assertThrows(CoreException.class, () -> {
                product.decreaseQuantity(new Quantity(1));
            });
            
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("재고가 부족합니다");
        }
    }

    @DisplayName("좋아요 수 설정")
    @Nested
    class SetLikeCount {
        
        @DisplayName("좋아요 수를 정상적으로 설정한다")
        @Test
        void setsLikeCount_whenValidCount() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 0L);
            
            // act
            product.setLikeCount(5L);
            
            // assert
            assertThat(product.getTotalLikeCount()).isEqualTo(5L);
        }

        @DisplayName("좋아요 수를 0으로 설정할 수 있다")
        @Test
        void setsLikeCount_toZero() {
            // arrange
            Product product = new Product("제목", new Brand("Apple"), new Money(10000), new Quantity(10), 10L);
            
            // act
            product.setLikeCount(0L);
            
            // assert
            assertThat(product.getTotalLikeCount()).isEqualTo(0L);
        }
    }
}
