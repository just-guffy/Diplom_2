package user;

import static io.restassured.RestAssured.given;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import model.Credentials;
import model.User;

public class UserService {

    private String baseURI;

    public UserService (String baseURI) {
        this.baseURI = baseURI;
    }

    @Step("Создание пользователя")
    public ValidatableResponse createUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(user)
                .post("/api/auth/register")
                .then()
                .log()
                .all();
    }

    @Step("Удаление пользователя")
    public ValidatableResponse deleteUser(String accessToken) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .delete("/api/auth/user")
                .then()
                .log()
                .all();
    }

    @Step("Логин пользователя")
    public ValidatableResponse login(Credentials credentials) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(credentials)
                .post("/api/auth/login")
                .then()
                .log()
                .all();
    }

    @Step("Получение данных пользователя")
    public ValidatableResponse getUserInfo(String accessToken) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .get("/api/auth/user")
                .then()
                .log()
                .all();
    }

    @Step("Обновление данных пользователя")
    public ValidatableResponse updateUserInfo(String accessToken, User updatedUser) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .header("Authorization", accessToken)
                .body(updatedUser)
                .patch("/api/auth/user")
                .then()
                .log()
                .all();
    }

    @Step("Обновление данных пользователя без авторизации")
    public ValidatableResponse updateUserInfoUnauthorized(User updatedUser) {
        return given()
                .filter(new AllureRestAssured())
                .log()
                .all()
                .baseUri(baseURI)
                .header("Content-Type", "application/json")
                .body(updatedUser)
                .patch("/api/auth/user")
                .then()
                .log()
                .all();
    }

}
