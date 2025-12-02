package com.loopers.domain.point;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.common.Money;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import com.loopers.support.error.CoreException;

import com.loopers.support.error.ErrorType;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointTest {
    @DisplayName("포인트 생성")
    @Nested
    class Create {

        @DisplayName("사용자가 정상일 때 Point가 생성된다")
        @Test
        void createsPoint_whenUserIsValid() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            
            // act
            Point point = Point.create(user);

            // assert
            assertAll(
                () -> assertThat(point).isNotNull(),
                () -> assertThat(point.getUser()).isEqualTo(user),
                () -> assertThat(point.getBalance().value()).isEqualTo(0L)
            );
        }

        @DisplayName("생성된 Point의 balance가 0원이다")
        @Test
        void createsPoint_withZeroBalance() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            
            // act
            Point point = Point.create(user);

            // assert
            assertThat(point.getBalance().value()).isEqualTo(0L);
        }
    }

    @DisplayName("포인트 충전")
    @Nested
    class Charge {

        @DisplayName("충전 금액이 0원이면 BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsException_whenChargeAmountIsZero() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            
            // act & assert
            CoreException result = assertThrows(CoreException.class, () -> {
                point.charge(new Money(0));
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("충전 금액은 0원보다 커야 합니다");
        }

        @DisplayName("충전 금액이 음수이면 예외가 발생한다")
        @Test
        void throwsException_whenChargeAmountIsNegative() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            
            // act & assert
            CoreException result = assertThrows(CoreException.class, () -> {
                point.charge(new Money(-1));
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("충전 금액이 정상일 때 balance가 증가한다")
        @Test
        void increasesBalance_whenChargeAmountIsValid() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            Money chargeAmount = new Money(10000);
            
            // act
            point.charge(chargeAmount);

            // assert
            assertThat(point.getBalance().value()).isEqualTo(10000L);
        }

        @DisplayName("여러 번 충전했을 때 balance가 누적된다")
        @Test
        void accumulatesBalance_whenChargedMultipleTimes() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            
            // act
            point.charge(new Money(5000));
            point.charge(new Money(3000));
            point.charge(new Money(2000));

            // assert
            assertThat(point.getBalance().value()).isEqualTo(10000L);
        }

        @DisplayName("충전 후 balance가 정확히 계산된다")
        @Test
        void calculatesBalanceCorrectly_afterCharge() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            long initialBalance = point.getBalance().value();
            Money chargeAmount = new Money(15000);
            
            // act
            point.charge(chargeAmount);

            // assert
            assertThat(point.getBalance().value()).isEqualTo(initialBalance + chargeAmount.value());
        }
    }

    @DisplayName("포인트 사용")
    @Nested
    class Use {

        @DisplayName("잔액보다 많은 금액을 사용하려고 하면 BAD_REQUEST 예외가 발생한다")
        @Test
        void throwsException_whenUseAmountExceedsBalance() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            point.charge(new Money(1000));
            
            // act & assert
            CoreException result = assertThrows(CoreException.class, () -> {
                point.use(new Money(2000));
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("포인트가 부족합니다");
        }

        @DisplayName("잔액과 동일한 금액을 사용하면 성공한다")
        @Test
        void usesPoint_whenAmountEqualsBalance() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            point.charge(new Money(5000));
            
            // act
            point.use(new Money(5000));

            // assert
            assertThat(point.getBalance().value()).isEqualTo(0L);
        }

        @DisplayName("잔액보다 적은 금액을 사용하면 성공한다")
        @Test
        void usesPoint_whenAmountIsLessThanBalance() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            point.charge(new Money(10000));
            
            // act
            point.use(new Money(3000));

            // assert
            assertThat(point.getBalance().value()).isEqualTo(7000L);
        }

        @DisplayName("사용 후 balance가 정확히 감소한다")
        @Test
        void decreasesBalanceCorrectly_afterUse() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            point.charge(new Money(10000));
            long initialBalance = point.getBalance().value();
            Money useAmount = new Money(4000);
            
            // act
            point.use(useAmount);

            // assert
            assertThat(point.getBalance().value()).isEqualTo(initialBalance - useAmount.value());
        }

        @DisplayName("여러 번 사용했을 때 balance가 정확히 감소한다")
        @Test
        void decreasesBalance_whenUsedMultipleTimes() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            point.charge(new Money(10000));
            
            // act
            point.use(new Money(2000));
            point.use(new Money(3000));
            point.use(new Money(1000));

            // assert
            assertThat(point.getBalance().value()).isEqualTo(4000L);
        }

        @DisplayName("잔액이 0원일 때 사용하려고 하면 예외가 발생한다")
        @Test
        void throwsException_whenBalanceIsZero() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            
            // act & assert
            CoreException result = assertThrows(CoreException.class, () -> {
                point.use(new Money(1000));
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
            assertThat(result.getMessage()).contains("포인트가 부족합니다");
        }
    }

    @DisplayName("통합 시나리오 테스트")
    @Nested
    class IntegrationScenarios {

        @DisplayName("충전 → 사용 → 충전 → 사용 순서로 정상 동작한다")
        @Test
        void worksCorrectly_chargeUseChargeUse() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            
            // act
            point.charge(new Money(10000));
            point.use(new Money(3000));
            point.charge(new Money(5000));
            point.use(new Money(2000));

            // assert
            assertThat(point.getBalance().value()).isEqualTo(10000L);
        }

        @DisplayName("잔액이 부족한 상태에서 충전 후 사용이 가능하다")
        @Test
        void allowsUse_afterChargeWhenInsufficient() {
            // arrange
            User user = new User(new UserId("user123"), new Email("email@email.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            Point point = Point.create(user);
            point.charge(new Money(1000));
            
            // act & assert - 잔액 부족으로 사용 실패
            CoreException exception = assertThrows(CoreException.class, () -> {
                point.use(new Money(2000));
            });
            assertThat(exception.getMessage()).contains("포인트가 부족합니다");
            
            // 충전 후 사용 성공
            point.charge(new Money(2000));
            point.use(new Money(2000));
            
            // assert
            assertThat(point.getBalance().value()).isEqualTo(1000L);
        }
    }
}
