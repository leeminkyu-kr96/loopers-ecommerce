package com.loopers.domain.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.common.Money;
import com.loopers.domain.common.Quantity;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LikeServiceIntegrationTest {
    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("좋아요 등록/취소")
    @Nested
    class LikeManagement {

        @DisplayName("좋아요 등록이 정상 처리된다")
        @Test
        void createsLike_whenAddLikeIsCalled() {
            // arrange
            User user = userJpaRepository.save(
                    new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE,
                            new BirthDate(LocalDate.of(1999, 1, 1))));
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));

            // act
            likeService.addLike(user, product);

            // assert
            boolean isLiked = likeService.isLiked(user, product);
            assertThat(isLiked).isTrue();
        }

        @DisplayName("좋아요 취소가 정상 처리된다")
        @Test
        void removesLike_whenRemoveLikeIsCalled() {
            // arrange
            User user = userJpaRepository.save(
                    new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE,
                            new BirthDate(LocalDate.of(1999, 1, 1))));
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));
            likeService.addLike(user, product);

            // act
            likeService.removeLike(user, product);

            // assert
            boolean isLiked = likeService.isLiked(user, product);
            assertThat(isLiked).isFalse();
        }

        @DisplayName("좋아요 등록: 이미 좋아요가 있으면 취소한다")
        @Test
        void cancelsLike_whenAddLikeIsCalledOnLikedProduct() {
            // arrange
            User user = userJpaRepository.save(
                    new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE,
                            new BirthDate(LocalDate.of(1999, 1, 1))));
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));
            likeService.addLike(user, product); // 첫 번째 호출: 좋아요 추가
            assertThat(likeService.isLiked(user, product)).isTrue();

            // act
            likeService.addLike(user, product); // 두 번째 호출: 좋아요 취소

            // assert
            assertThat(likeService.isLiked(user, product)).isFalse();
        }

        @DisplayName("좋아요 취소: 이미 좋아요가 없으면 추가한다")
        @Test
        void addsLike_whenRemoveLikeIsCalledOnUnlikedProduct() {
            // arrange
            User user = userJpaRepository.save(
                    new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE,
                            new BirthDate(LocalDate.of(1999, 1, 1))));
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));
            // 처음부터 좋아요 없음
            assertThat(likeService.isLiked(user, product)).isFalse();

            // act
            likeService.removeLike(user, product); // 좋아요 추가

            // assert
            assertThat(likeService.isLiked(user, product)).isTrue();
        }

        @DisplayName("좋아요 토글이 정상 동작한다")
        @Test
        void togglesLike_whenToggleLikeIsCalled() {
            // arrange
            User user = userJpaRepository.save(
                    new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE,
                            new BirthDate(LocalDate.of(1999, 1, 1))));
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));

            // act & assert - 첫 번째 호출: 좋아요 등록
            likeService.toggleLike(user, product);
            assertThat(likeService.isLiked(user, product)).isTrue();

            // act & assert - 두 번째 호출: 좋아요 취소
            likeService.toggleLike(user, product);
            assertThat(likeService.isLiked(user, product)).isFalse();
        }
    }

    @DisplayName("좋아요 수 조회")
    @Nested
    class LikeCount {

        @DisplayName("상품의 좋아요 수를 정확히 조회한다")
        @Test
        void returnsCorrectLikeCount_whenMultipleUsersLikeProduct() {
            // arrange
            User user1 = userJpaRepository.save(
                    new User(new UserId("user1"), new Email("user1@user.com"), Gender.MALE,
                            new BirthDate(LocalDate.of(1999, 1, 1))));
            User user2 = userJpaRepository.save(
                    new User(new UserId("user2"), new Email("user2@user.com"), Gender.FEMALE,
                            new BirthDate(LocalDate.of(2000, 1, 1))));
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));
            likeService.addLike(user1, product);
            likeService.addLike(user2, product);

            // act
            long likeCount = likeService.getLikeCount(product);

            // assert
            assertThat(likeCount).isEqualTo(2L);
        }
    }

    @DisplayName("동시성 테스트")
    @Nested
    class ConcurrencyTest {

        @DisplayName("동일한 상품에 대해 여러명이 좋아요를 요청해도, 상품의 좋아요 개수가 정상 반영되어야 한다.")
        @Test
        void addLike_concurrency() throws InterruptedException {
            // arrange
            Product product = productJpaRepository.save(
                    new Product("product123", new Brand("Apple"), new Money(10000), new Quantity(100), 0L));

            int numberOfUsers = 50;
            List<User> users = new ArrayList<>();
            for (int i = 0; i < numberOfUsers; i++) {
                User user = userJpaRepository.save(
                        new User(new UserId("user" + i), new Email("user" + i + "@user.com"), Gender.MALE,
                                new BirthDate(LocalDate.of(1999, 1, 1))));
                users.add(user);
            }

            ExecutorService executorService = Executors.newFixedThreadPool(numberOfUsers);
            CountDownLatch latch = new CountDownLatch(numberOfUsers);

            // act
            for (User user : users) {
                executorService.submit(() -> {
                    try {
                        likeService.addLike(user, product);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executorService.shutdown();

            // assert
            long likeCount = likeService.getLikeCount(product);
            assertThat(likeCount).isEqualTo(numberOfUsers);
        }
    }
}
