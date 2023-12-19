package bankmonitor.controller;

import bankmonitor.AbstractIntegrationTest;
import bankmonitor.dto.Transaction;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpStatus;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import static io.restassured.RestAssured.given;


class TransactionControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String PATH_TRANSACTIONS = "/transactions";

    @Test
    @Order(1)
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
            "{ \"amount\": 54321, \"sender\": \"Bankmonitor\", \"recipient\": \"John Doe\", \"dynamicField\": \"something\" }",
            "{ \"amount\": 54321, \"sender\": \"Bankmonitor\", \"recipient\": \"John Doe\", \"dynamicFieldNested\": { \"dynamicField\": \"testValue\"} }",
    })
    public void createTransactionTest(String data) {
        //given + when
        Transaction response = given()
                .contentType(ContentType.JSON)
                .when()
                .body(data)
                .post(PATH_TRANSACTIONS)
                .then()
                .assertThat().statusCode(HttpStatus.CREATED.value())
                .extract().as(Transaction.class);

        //then
        Transaction result = given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH_TRANSACTIONS + "/{id}", response.id())
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(Transaction.class);

        JSONAssert.assertEquals(data, result.data(), true);
    }

    @Test
    public void updateTransactionTest() {
        //given + when
        Transaction createdTransaction = given()
                .contentType(ContentType.JSON)
                .when()
                .body("{ \"amount\": 3333, \"reference\": \"\", \"sender\": \"Bankmonitor\" }")
                .post(PATH_TRANSACTIONS)
                .then()
                .assertThat().statusCode(HttpStatus.CREATED.value())
                .extract().as(Transaction.class);

        //when
        String updatedData = "{ \"amount\": 10000, \"reference\": \"NewRef\" }";
        given()
                .contentType(ContentType.JSON)
                .when()
                .body(updatedData)
                .put(PATH_TRANSACTIONS + "/{id}", createdTransaction.id())
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(Transaction.class);

        //then
        Transaction result = given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH_TRANSACTIONS + "/{id}", createdTransaction.id())
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(Transaction.class);

        String expected = "{ \"amount\": 10000, \"reference\": \"NewRef\", \"sender\": \"Bankmonitor\" }";
        JSONAssert.assertEquals(expected, result.data(), true);
    }

    @Test
    public void patchTransactionTest() {
        //given + when
        Transaction createdTransaction = given()
                .contentType(ContentType.JSON)
                .when()
                .body("{ \"amount\": 3333, \"reference\": \"\", \"sender\": \"Bankmonitor\" }")
                .post(PATH_TRANSACTIONS)
                .then()
                .assertThat().statusCode(HttpStatus.CREATED.value())
                .extract().as(Transaction.class);

        //when
        String updatedData = "{ \"amount\": 10000, \"reference\": \"NewRef\" }";
        given()
                .contentType(ContentType.JSON)
                .when()
                .body(updatedData)
                .patch(PATH_TRANSACTIONS + "/{id}", createdTransaction.id())
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(Transaction.class);

        //then
        Transaction result = given()
                .contentType(ContentType.JSON)
                .when()
                .get(PATH_TRANSACTIONS + "/{id}", createdTransaction.id())
                .then()
                .assertThat().statusCode(HttpStatus.OK.value())
                .extract().as(Transaction.class);

        String expected = "{ \"amount\": 10000, \"reference\": \"NewRef\", \"sender\": \"Bankmonitor\" }";
        JSONAssert.assertEquals(expected, result.data(), true);
    }

    @Test
    public void updateTransactionWithUnknownIdTest() {
        //given
        Long unknownId = 9999999L;
        String updatedData = "{ \"amount\": 10000, \"reference\": \"NewRef\" }";

        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .body(updatedData)
                .put(PATH_TRANSACTIONS + "/{id}", unknownId)
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response().asString();

        JSONAssert.assertEquals("{\"message\": \"TransactionEntity with id: 9999999 not found\"}", response, true);
    }

    @Test
    public void createTransactionWithLargeReferenceTest() {
        //given + when
        String largeRef = RandomStringUtils.random(999, true, true);
        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .body("{ \"amount\": 3333, \"reference\": \" "+ largeRef + "\", \"sender\": \"Bankmonitor\" }")
                .post(PATH_TRANSACTIONS)
                .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response().asString();

        JSONAssert.assertEquals("{\"message\": \"Invalid json data content size maximum: 1000, current: 1053\"}", response, true);
    }

    @Test
    public void updateTransactionWithLargeSizeTest() {
        //given + when
        Transaction createdTransaction = given()
                .contentType(ContentType.JSON)
                .when()
                .body("{ \"amount\": 3333, \"reference\": \"\", \"sender\": \"Bankmonitor\" }")
                .post(PATH_TRANSACTIONS)
                .then()
                .assertThat().statusCode(HttpStatus.CREATED.value())
                .extract().as(Transaction.class);

        String largeRef = RandomStringUtils.random(999, true, true);

        //when
        String updatedData = "{ \"amount\": 10000, \"reference\": \"" + largeRef + "\" }";
        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .body(updatedData)
                .put(PATH_TRANSACTIONS + "/{id}", createdTransaction.id())
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().response().asString();

        JSONAssert.assertEquals("{\"message\": \"Invalid json data content size maximum: 1000, current: 1053\"}", response, true);
    }

}
