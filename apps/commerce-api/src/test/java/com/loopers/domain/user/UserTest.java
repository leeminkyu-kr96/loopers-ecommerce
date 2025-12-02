package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {
    @DisplayName("회원가입 시 User 객체를 생성할 때, ")
    @Nested
    class Create {

        @DisplayName("입력한 ID 가 비어있으면, User 객체 생성에 실패한다.")
        @Test
        void createsUserModel_whenUserIdIsBlank() {
            // arrange
            String userId = "   ";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(new UserId(userId), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }


        @DisplayName("ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void createUserModel_whenUserIdIsNotValid() {
            // arrange
            String userId = "user123123123";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(new UserId(userId), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            });

            //assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }

        @DisplayName("입력한 이메일이 비어있으면, User 객체 생성에 실패한다.")
        @Test
        void createsUserModel_whenEmailIsBlank() {
            // arrange
            String email = "   ";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(new UserId("userId"), new Email(email), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void createUserModel_whenEmailIsNotValid() {
            // arrange
            String email = "user123123123";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(new UserId("user123"), new Email(email), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            });

            //assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }

        @DisplayName("생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void createUserModel_whenBirthDateIsNotValid() {
            // arrange
            String birthDate = "19991-01-01";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(new UserId("user123"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            });

            //assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }

        @DisplayName("입력한 생년월일이 null이면, User 객체 생성에 실패한다.")
        @Test
        void createsUserModel_whenBirthDateIsNull() {
            // arrange
            String birthDate = null;
            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new User(new UserId("userId"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("모든 필드가 정상일 때 User가 생성된다")
        @Test
        void createsUser_whenAllFieldsAreValid() {
            // arrange
            UserId userId = new UserId("user123");
            Email email = new Email("user123@example.com");
            Gender gender = Gender.MALE;
            BirthDate birthDate = new BirthDate(LocalDate.of(1999, 1, 1));

            // act
            User user = new User(userId, email, gender, birthDate);

            // assert
            assertAll(
                () -> assertThat(user.getUserId()).isEqualTo(userId),
                () -> assertThat(user.getEmail()).isEqualTo(email),
                () -> assertThat(user.getGender()).isEqualTo(gender),
                () -> assertThat(user.getBirthDate()).isEqualTo(birthDate)
            );
        }

        @DisplayName("생성된 User의 모든 필드가 올바르게 설정된다")
        @Test
        void setsAllFieldsCorrectly_whenCreated() {
            // arrange
            UserId userId = new UserId("user456");
            Email email = new Email("user456@test.com");
            Gender gender = Gender.FEMALE;
            BirthDate birthDate = new BirthDate(LocalDate.of(2000, 6, 15));

            // act
            User user = new User(userId, email, gender, birthDate);

            // assert
            assertAll(
                () -> assertThat(user.getUserId().userId()).isEqualTo("user456"),
                () -> assertThat(user.getEmail().email()).isEqualTo("user456@test.com"),
                () -> assertThat(user.getGender()).isEqualTo(Gender.FEMALE),
                () -> assertThat(user.getBirthDate().birthDate()).isEqualTo(LocalDate.of(2000, 6, 15))
            );
        }
    }
}
