package bankmonitor.controller;

import bankmonitor.model.TransactionEntity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerIntegrationTest {

    private final static String BASE_URI = "http://localhost";
    public static final String PATH_TRANSACTIONS = "/transactions";

    @LocalServerPort
    private int port;

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            "postgres:15-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        POSTGRES.start();
    }

    @AfterAll
    static void afterAll() {
        POSTGRES.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void getTransactionsTest() {
        //given + when
        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH_TRANSACTIONS)
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().response().asString();

        //then
        JSONAssert.assertEquals("""
                [
                  {
                    "id": 1,
                    "data": "{ \\"amount\\": 100, \\"reference\\": \\"BM_2023_101\\" }",
                    "reference": "BM_2023_101",
                    "amount": 100
                  },
                  {
                    "id": 2,
                    "data": "{ \\"amount\\": 3333, \\"reference\\": \\"\\", \\"sender\\": \\"Bankmonitor\\" }",
                    "reference": "",
                    "amount": 3333
                  },
                  {
                    "id": 3,
                    "data": "{ \\"amount\\": -100, \\"reference\\": \\"BM_2023_101_BACK\\", \\"reason\\": \\"duplicate\\" }",
                    "reference": "BM_2023_101_BACK",
                    "amount": -100
                  },
                  {
                    "id": 4,
                    "data": "{ \\"amount\\": 12345, \\"reference\\": \\"BM_2023_105\\" }",
                    "reference": "BM_2023_105",
                    "amount": 12345
                  },
                  {
                    "id": 5,
                    "data": "{ \\"amount\\": 54321, \\"sender\\": \\"Bankmonitor\\", \\"recipient\\": \\"John Doe\\" }",
                    "reference": "",
                    "amount": 54321
                  }
                ]
                """, response, true);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{ \"amount\": 100, \"reference\": \"BM_2023_101\" }",
            "{ \"amount\": 3333, \"reference\": \"\", \"sender\": \"Bankmonitor\" }",
            "{ \"amount\": -100, \"reference\": \"BM_2023_101_BACK\", \"reason\": \"duplicate\" }",
            "{ \"amount\": 12345, \"reference\": \"BM_2023_105\" }",
            "{ \"amount\": 54321, \"sender\": \"Bankmonitor\", \"recipient\": \"John Doe\" }",
            })
    public void createTransactionTest(String data) {
        //given + when
        TransactionEntity response = given()
                .contentType(ContentType.JSON)
                .when()
                .body(data)
                .post(PATH_TRANSACTIONS)
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(TransactionEntity.class);

        //then
        TransactionEntity result = given()
                .contentType(ContentType.JSON)
                .when()
                .body(data)
                .get(PATH_TRANSACTIONS + "/{id}", response.getId())
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(TransactionEntity.class);

        JSONAssert.assertEquals(data, result.getData(), true);

    }

    @Test
    public void updateTransactionTest() {

    }

}
