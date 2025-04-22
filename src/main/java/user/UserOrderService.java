package user;

import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;

public class UserOrderService {
    private String baseURI;

    public UserOrderService(String baseURI) {
        this.baseURI = baseURI;
    }

    @Step("Создание заказа с авторизацией")
    public ValidatableResponse createOrderWithAuth(String accessToken, String[] ingredients) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .body(new OrderRequest(ingredients))
                .post("/api/orders")
                .then()
                .log()
                .all();
    }

    @Step("Создание заказа без авторизации")
    public ValidatableResponse createOrderWithoutAuth(String[] ingredients) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(new OrderRequest(ingredients))
                .post("/api/orders")
                .then()
                .log()
                .all();
    }

    @Step("Получение заказов авторизованного пользователя")
    public ValidatableResponse getUserOrdersWithAuth(String accessToken) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .get("/api/orders")
                .then()
                .log()
                .all();
    }

    @Step("Получение заказов без авторизации")
    public ValidatableResponse getUserOrdersWithoutAuth() {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .get("/api/orders")
                .then()
                .log()
                .all();
    }

    // Внутренний класс для формирования тела запроса
    private static class OrderRequest {
        private final String[] ingredients;

        public OrderRequest(String[] ingredients) {
            this.ingredients = ingredients;
        }
    }
}