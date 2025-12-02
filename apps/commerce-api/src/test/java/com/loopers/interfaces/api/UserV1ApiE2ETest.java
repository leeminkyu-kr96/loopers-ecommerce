package com.loopers.interfaces.api;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {

    private static final String ENDPOINT_SIGNUP = "/api/v1/users";
    private static final String ENDPOINT_GET_ME = "/api/v1/users/me";
    private static final Function<String, String> ENDPOINT_GET = userId -> "/api/v1/users/" + userId;

    private final TestRestTemplate testRestTemplate;
    private final UserRepository userRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public UserV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        UserRepository userRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userRepository = userRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /*
    회원가입
    - [x]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
    - [x]  회원 가입 시에 필수 필드가 없을 경우, `400 Bad Request` 응답을 반환한다.

    내 정보 조회
    - [x]  내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
    - [x]  존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.
     */

    @DisplayName("POST /api/v1/users/signup")
    @Nested
    class Signup {
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenSignupIsSuccessful() {
            // arrange
            UserV1Dto.SignupRequest request = new UserV1Dto.SignupRequest(
                "user123",
                "user123@example.com",
                "male",
                "1999-01-01"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_SIGNUP, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo("user123"),
                () -> assertThat(response.getBody().data().email()).isEqualTo("user123@example.com"),
                () -> assertThat(response.getBody().data().birthDate()).isEqualTo("1999-01-01"),
                () -> assertThat(response.getBody().data().gender()).isEqualTo("MALE")
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenRequiredFieldIsMissing() {
            // arrange - gender 필드를 null로 설정
            UserV1Dto.SignupRequest request = new UserV1Dto.SignupRequest(
                "user123",
                "user123@example.com",
                null,
                "1999-01-01"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_SIGNUP, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("회원 가입 시 ID가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, 400 Bad Request 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenUserIdFormatIsInvalid() {
            // arrange - 10자 초과 ID
            UserV1Dto.SignupRequest request = new UserV1Dto.SignupRequest(
                "user123456789", // 13자
                "user123@example.com",
                "male",
                "1999-01-01"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_SIGNUP, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("회원 가입 시 이메일이 `xx@yy.zz` 형식에 맞지 않으면, 400 Bad Request 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenEmailFormatIsInvalid() {
            // arrange - 잘못된 이메일 형식
            UserV1Dto.SignupRequest request = new UserV1Dto.SignupRequest(
                "user123",
                "invalid-email",
                "male",
                "1999-01-01"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_SIGNUP, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("회원 가입 시 생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, 400 Bad Request 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenBirthDateFormatIsInvalid() {
            // arrange - 잘못된 생년월일 형식
            UserV1Dto.SignupRequest request = new UserV1Dto.SignupRequest(
                "user123",
                "user123@example.com",
                "male",
                "1999/01/01" // 잘못된 형식
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_SIGNUP, HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class GetMe {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenValidUserIdHeaderIsProvided() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(user.getUserId().userId()),
                () -> assertThat(response.getBody().data().email()).isEqualTo(user.getEmail().email()),
                () -> assertThat(response.getBody().data().birthDate()).isEqualTo(user.getBirthDate().birthDate()),
                () -> assertThat(response.getBody().data().gender()).isEqualTo(user.getGender().name())
            );
        }

        @DisplayName("`X-USER-ID` 헤더가 없을 경우, 400 Bad Request 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            // X-USER-ID 헤더를 의도적으로 설정하지 않음

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("존재하지 않는 ID로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void throwsNotFoundException_whenUserIdDoesNotExist() {
            // arrange
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("X-USER-ID", "nonexistent");

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }
    }

    @DisplayName("GET /api/v1/users/{userId}")
    @Nested
    class GetUser {
        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnsUserInfo_whenValidUserIdIsProvided() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            String requestUrl = ENDPOINT_GET.apply(user.getUserId().userId());

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(user.getUserId().userId()),
                () -> assertThat(response.getBody().data().email()).isEqualTo(user.getEmail().email()),
                () -> assertThat(response.getBody().data().birthDate()).isEqualTo(user.getBirthDate().birthDate()),
                () -> assertThat(response.getBody().data().gender()).isEqualTo(user.getGender().name())
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void throwsNotFoundException_whenUserIdDoesNotExist() {
            // arrange
            String invalidUserId = "nonexistent";
            String requestUrl = ENDPOINT_GET.apply(invalidUserId);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(null), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }
    }
}
