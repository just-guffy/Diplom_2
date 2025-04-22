import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.User;
import model.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserOrderService;
import user.UserService;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class GetUserOrdersTest {
    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    private static final String[] INGREDIENTS = {"61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72"};

    private User user;
    private UserService userService;
    private UserOrderService orderService;
    private String userAccessToken;

    @Before
    public void setUp() {
        userService = new UserService(BASE_URI);
        orderService = new UserOrderService(BASE_URI);

        user = new User("user-test120919913@yandex.ru", "1234", "Alex");
        ValidatableResponse response = userService.createUser(user);

        // Проверка статуса и success
        response.assertThat()
                .statusCode(200)
                .body("success", is(true));

        // Проверка, что accessToken существует
        response.assertThat()
                .body("accessToken", notNullValue());

        // Извлекаем accessToken из ответа
        userAccessToken = response.extract().path("accessToken");

        // Авторизуем пользователя
        Credentials credentials = Credentials.fromUser(user);
        userService.login(credentials);

        // Создаем заказ, чтобы у пользователя была история заказов
        orderService.createOrderWithAuth(userAccessToken, INGREDIENTS);
    }

    @Test
    @Step("Получение заказов авторизованного пользователя")
    public void getUserOrdersWithAuthShouldReturnOrders() {
        ValidatableResponse response = orderService.getUserOrdersWithAuth(userAccessToken);

        response.assertThat()
                .statusCode(200)
                .body("success", is(true),
                        "orders", not(empty()));

        // Проверяем каждый заказ в списке
        List<Map<String, Object>> orders = response.extract().jsonPath().getList("orders");
        for (Map<String, Object> order : orders) {

            assertThat((List<?>) order.get("ingredients"), not(empty()));
            assertThat(order.get("_id"), notNullValue());
            assertThat(order.get("status").toString(), not(emptyOrNullString()));
            assertThat(order.get("number"), notNullValue());
            assertThat(order.get("createdAt"), notNullValue());
            assertThat(order.get("updatedAt"), notNullValue());
        }
    }

    @Test
    @Step("Получение заказов без авторизации")
    public void getUserOrdersWithoutAuthShouldReturnError() {
        ValidatableResponse response = orderService.getUserOrdersWithoutAuth();

        response.assertThat()
                .statusCode(401)
                .body("success", is(false),
                        "message", equalTo("You should be authorised"));
    }

    @After
    public void deleteUser() {
        if (userAccessToken != null && !userAccessToken.isEmpty()) { // Проверяем, есть ли accessToken
            userService.deleteUser(userAccessToken); // Удаление пользователя
        }
    }
}