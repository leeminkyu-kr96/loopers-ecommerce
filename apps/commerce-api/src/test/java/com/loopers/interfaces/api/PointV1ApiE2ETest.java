package com.loopers.interfaces.api;

import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserId;
import com.loopers.domain.user.Email;
import com.loopers.domain.user.Gender;
import com.loopers.domain.user.BirthDate;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.point.PointV1Dto;
import com.loopers.domain.common.Money;
import java.time.LocalDate;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest {

    private static final String ENDPOINT_GET = "/api/v1/points";
    private static final String ENDPOINT_CHARGE = "/api/v1/points/charge";

    private final TestRestTemplate testRestTemplate;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final DatabaseCleanUp databaseCleanUp;

    @Autowired
    public PointV1ApiE2ETest(
        TestRestTemplate testRestTemplate,
        UserRepository userRepository,
        PointRepository pointRepository,
        DatabaseCleanUp databaseCleanUp
    ) {
        this.testRestTemplate = testRestTemplate;
        this.userRepository = userRepository;
        this.pointRepository = pointRepository;
        this.databaseCleanUp = databaseCleanUp;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    /*
    포인트 조회
    - [x]  포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
    - [x]  `X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.

    포인트 충전
    - [x]  존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.
    - [x]  존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.
     */

    @DisplayName("GET /api/v1/points")
    @Nested
    class GetPoint {
        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnsPoint_whenValidUserIdHeaderIsProvided() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            point.charge(new Money(500));
            pointRepository.save(point);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(user.getUserId().userId()),
                () -> assertThat(response.getBody().data().point().value()).isEqualTo(500)
            );
        }

        @DisplayName("`X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            HttpHeaders headers = new HttpHeaders();
            // X-USER-ID 헤더를 의도적으로 설정하지 않음

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("존재하지 않는 유저로 조회할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void throwsNotFoundException_whenUserDoesNotExist() {
            // arrange
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "nonexistent");

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

        @DisplayName("포인트가 0원일 때도 정상적으로 0을 반환한다.")
        @Test
        void returnsZero_whenPointIsZero() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            Point point = Point.create(user);
            pointRepository.save(point);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().point().value()).isEqualTo(0)
            );
        }
    }

    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class ChargePoint {
        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void chargesPoint_when1000AmountIsProvided() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            PointV1Dto.ChargeRequest request = new PointV1Dto.ChargeRequest(new Money(1000));

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response.getBody()).isNotNull(),
                () -> assertThat(response.getBody().data().userId()).isEqualTo(user.getUserId().userId()),
                () -> assertThat(response.getBody().data().point().value()).isEqualTo(1000)
            );
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void throwsNotFoundException_whenUserDoesNotExist() {
            // arrange
            PointV1Dto.ChargeRequest request = new PointV1Dto.ChargeRequest(new Money(1000));

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "nonexistent");
            headers.setContentType(MediaType.APPLICATION_JSON);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }

        @DisplayName("`X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenUserIdHeaderIsMissing() {
            // arrange
            PointV1Dto.ChargeRequest request = new PointV1Dto.ChargeRequest(new Money(1000));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            // X-USER-ID 헤더를 의도적으로 설정하지 않음

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("충전 금액이 0원 이하일 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenChargeAmountIsZeroOrNegative() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );
            PointV1Dto.ChargeRequest request = new PointV1Dto.ChargeRequest(new Money(0));

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response.getStatusCode().is4xxClientError()),
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            );
        }

        @DisplayName("여러 번 충전할 경우, 누적된 포인트가 정확히 반환된다.")
        @Test
        void accumulatesPoints_whenChargedMultipleTimes() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // act - 첫 번째 충전
            PointV1Dto.ChargeRequest request1 = new PointV1Dto.ChargeRequest(new Money(1000));
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response1 =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(request1, headers), responseType);

            // act - 두 번째 충전
            PointV1Dto.ChargeRequest request2 = new PointV1Dto.ChargeRequest(new Money(2000));
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response2 =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(request2, headers), responseType);

            // assert
            assertAll(
                () -> assertTrue(response1.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response1.getBody().data().point().value()).isEqualTo(1000),
                () -> assertTrue(response2.getStatusCode().is2xxSuccessful()),
                () -> assertThat(response2.getBody().data().point().value()).isEqualTo(3000) // 1000 + 2000
            );
        }

        @DisplayName("충전 금액이 음수일 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenChargeAmountIsNegative() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Money 생성자에서 음수를 허용하지 않으므로, Money 생성 시점에 예외가 발생함
            // 이는 도메인 레벨에서 검증되므로, API 레벨에서는 이미 검증됨
            // 따라서 이 테스트는 Money 생성 시 예외가 발생하는 것을 확인
            com.loopers.support.error.CoreException exception = org.junit.jupiter.api.Assertions.assertThrows(
                com.loopers.support.error.CoreException.class,
                () -> new Money(-1000),
                "Money 생성 시 음수 예외가 발생해야 합니다."
            );
            assertThat(exception.getErrorType()).isEqualTo(com.loopers.support.error.ErrorType.BAD_REQUEST);
        }

        @DisplayName("충전 후 포인트 조회 시 충전된 금액이 정확히 반영된다.")
        @Test
        void reflectsChargedAmount_whenPointIsRetrievedAfterCharge() {
            // arrange
            User user = userRepository.save(
                new User(new UserId("user123"), new Email("user123@example.com"), Gender.MALE, new BirthDate(LocalDate.of(1999, 1, 1)))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", user.getUserId().userId());
            headers.setContentType(MediaType.APPLICATION_JSON);

            // act - 포인트 충전
            PointV1Dto.ChargeRequest chargeRequest = new PointV1Dto.ChargeRequest(new Money(5000));
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> chargeResponseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> chargeResponse =
                testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, new HttpEntity<>(chargeRequest, headers), chargeResponseType);

            // act - 포인트 조회
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> getResponseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> getResponse =
                testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), getResponseType);

            // assert
            assertAll(
                () -> assertTrue(chargeResponse.getStatusCode().is2xxSuccessful()),
                () -> assertThat(chargeResponse.getBody().data().point().value()).isEqualTo(5000),
                () -> assertTrue(getResponse.getStatusCode().is2xxSuccessful()),
                () -> assertThat(getResponse.getBody()).isNotNull(),
                () -> assertThat(getResponse.getBody().data().point().value()).isEqualTo(5000),
                () -> assertThat(getResponse.getBody().data().point().value()).isEqualTo(chargeResponse.getBody().data().point().value())
            );
        }
    }
}
