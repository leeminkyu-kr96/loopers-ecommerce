package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LikeTest {
    @DisplayName("좋아요 생성")
    @Nested
    class Create {
        @DisplayName("좋아요 등록이 정상 처리된다")
        @Test
        void createsLikeModel_whenLikeIsCreated() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L);

            // act
            Like like = Like.create(user, product);

            // assert
            assertAll(
                () -> assertThat(like.getUser()).isEqualTo(user),
                () -> assertThat(like.getProduct()).isEqualTo(product)
            );
        }

        @DisplayName("사용자가 null이면 BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsException_whenUserIsNull() {
            // arrange
            Product product = new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L);

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                Like.create(null, product);
            });
            
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("사용자 정보는 필수입니다");
        }

        @DisplayName("상품이 null이면 BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsException_whenProductIsNull() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                Like.create(user, null);
            });
            
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(exception.getMessage()).contains("상품 정보는 필수입니다");
        }

        @DisplayName("사용자와 상품이 모두 정상일 때 Like가 생성된다")
        @Test
        void createsLike_whenUserAndProductAreValid() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Product product = new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L);

            // act
            Like like = Like.create(user, product);

            // assert
            assertAll(
                () -> assertThat(like).isNotNull(),
                () -> assertThat(like.getUser()).isEqualTo(user),
                () -> assertThat(like.getProduct()).isEqualTo(product)
            );
        }
    }
}
