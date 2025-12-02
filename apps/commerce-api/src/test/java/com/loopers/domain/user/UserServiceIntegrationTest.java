package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import com.loopers.infrastructure.user.UserRepositoryImpl;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @SpyBean
    private UserRepositoryImpl userRepositoryImpl;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원가입")
    @Nested
    class SignUp {

        @DisplayName("회원 가입시 User 저장이 수행된다. ( spy 검증 )")
        @Test
        void returnsUserInfo_whenSignUp() {
            // arrange
            User userModel = new User(new UserId("userId1"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));

            // act
            User user = userService.signUp(userModel);

            // assert

            verify(userRepositoryImpl, times(1)).save(any(User.class));

            assertAll(
                    () -> assertThat(user).isNotNull(),
                    () -> assertThat(user.getUserId()).isNotNull(),
                    () -> assertThat(user.getUserId()).isEqualTo("userId1"),
                    () -> assertThat(user.getEmail()).isEqualTo("user123@user.com"),
                    () -> assertThat(user.getBirthDate()).isEqualTo(LocalDate.of(1999, 1, 1)));

        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void throwsException_whenUserIdIsDuplicated() {
            // arrange
            User user = new User(new UserId("userId1"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            userService.signUp(user);

            User dupUser = new User(new UserId("userId1"), new Email("user1234@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 11)));

            // act
            CoreException exception = assertThrows(CoreException.class, () -> userService.signUp(dupUser));

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }
    }

    @DisplayName("내 정보 조회")
    @Nested
    class MyPage {
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnsUserInfo_whenValidIdIsProvided() {
            // arrange
            User user = new User(new UserId("userId1"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));
            userService.signUp(user);

            // act
            User result = userService.getUser(user.getUserId()).orElse(null);

            // assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getUserId()).isEqualTo(user.getUserId()),
                    () -> assertThat(result.getEmail()).isEqualTo(user.getEmail()),
                    () -> assertThat(result.getBirthDate()).isEqualTo(user.getBirthDate())
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenInvalidUserIdIsProvided() {
            // arrange
            User user = new User(new UserId("userId1"), new Email("user123@user.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)));

            // act
            User result = userService.getUser(user.getUserId()).orElse(null);

            // assert
            assertThat(result).isNull();
        }
    }
}
