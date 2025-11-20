package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.user.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import com.loopers.domain.user.User;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;

class LikeTest {
    @DisplayName("좋아요 행위 ")
    @Nested
    class Create {
        @DisplayName("좋아요 등록이 정상 처리된다")
        @Test
        void createsLikeModel_whenLikeIsCreated() {
            // arrange
            User user = new User(new UserId("user123"), new Email("user123@user.com"), new Gender("male"), new BirthDate("1999-01-01"));
            Product product = new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100));

            // act
            Like like = new Like(user, product);

            // assert
            assertAll(
                () -> assertThat(like.getUser()).isEqualTo(user),
                () -> assertThat(like.getProduct()).isEqualTo(product)
            );
        }
    }
}
