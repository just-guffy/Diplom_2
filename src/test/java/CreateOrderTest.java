import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import model.User;
import model.Credentials;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import user.UserOrderService;
import user.UserService;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CreateOrderTest {
    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    private String[] validIngredients;
    private static final String[] INVALID_INGREDIENTS = {"invalid_hash_1", "invalid_hash_2"};
    private static final String[] EMPTY_INGREDIENTS = {};

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

        // Авторизация пользователя
        Credentials credentials = new Credentials(user.getEmail(), user.getPassword());
        userService.login(credentials);

        // Получаем 2 валидных ингредиента перед тестами
        validIngredients = orderService.getTwoIngredients();
    }

    @Test
    @Step("Создание заказа с авторизацией и валидными ингредиентами")
    public void createOrderWithAuthAndValidIngredients() {
        ValidatableResponse response = orderService.createOrderWithAuth(userAccessToken, validIngredients);

        response.assertThat()
                .statusCode(200)
                .body("success", equalTo(true),
                        "name", notNullValue(),
                        "order.number", notNullValue());
    }

    @Test
    @Step("Создание заказа без авторизации с валидными ингредиентами")
    public void createOrderWithoutAuthWithValidIngredients() {
        ValidatableResponse response = orderService.createOrderWithoutAuth(validIngredients);

        response.assertThat()
                .statusCode(200)
                .body("success", equalTo(true),
                        "order.number", notNullValue(),
                        "name", notNullValue());
    }

    @Test
    @Step("Создание заказа с авторизацией без ингредиентов")
    public void createOrderWithAuthWithoutIngredients() {
        ValidatableResponse response = orderService.createOrderWithAuth(userAccessToken, EMPTY_INGREDIENTS);

        response.assertThat()
                .statusCode(400)
                .body("success", equalTo(false),
                        "message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @Step("Создание заказа с авторизацией и невалидными ингредиентами")
    public void createOrderWithAuthAndInvalidIngredients() {
        ValidatableResponse response = orderService.createOrderWithAuth(userAccessToken, INVALID_INGREDIENTS);

        response.assertThat()
                .statusCode(500);
    }

    @After
    public void deleteUser() {
        if (userAccessToken != null && !userAccessToken.isEmpty()) { // Проверяем, есть ли accessToken
            userService.deleteUser(userAccessToken); // Удаление пользователя
        }
    }
}