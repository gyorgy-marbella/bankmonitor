package bankmonitor.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerIntegrationTest {

    private final static String BASE_URI = "http://localhost";

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured() {
        RestAssured.baseURI = BASE_URI;
        RestAssured.port = port;
    }

    @Test
    public void getTransactionsTest() {
        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/transactions")
                .then()
                .assertThat().statusCode(200)
                .extract().response().asString();

        JSONAssert.assertEquals("""
                [
                  {
                    "data": "{ \\"amount\\": 100, \\"reference\\": \\"BM_2023_101\\" }",
                    "reference": "BM_2023_101",
                    "amount": 100
                  },
                  {
                    "data": "{ \\"amount\\": 3333, \\"reference\\": \\"\\", \\"sender\\": \\"Bankmonitor\\" }",
                    "reference": "",
                    "amount": 3333
                  },
                  {
                    "data": "{ \\"amount\\": -100, \\"reference\\": \\"BM_2023_101_BACK\\", \\"reason\\": \\"duplicate\\" }",
                    "reference": "BM_2023_101_BACK",
                    "amount": -100
                  },
                  {
                    "data": "{ \\"amount\\": 12345, \\"reference\\": \\"BM_2023_105\\" }",
                    "reference": "BM_2023_105",
                    "amount": 12345
                  },
                  {
                    "data": "{ \\"amount\\": 54321, \\"sender\\": \\"Bankmonitor\\", \\"recipient\\": \\"John Doe\\" }",
                    "reference": "",
                    "amount": 54321
                  }
                ]
                """, response, true);
    }

}
