import io.restassured.response.ValidatableResponse;
import io.qameta.allure.Step;
import model.User;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import user.UserService;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CreateUserTest {

    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";

    // Переменная для хранения accessToken
    private String userAccessToken;

    // Создаем экземпляр клиента
    private UserService userService = new UserService(BASE_URI);

    // Параметры для теста
    private final User user;
    private final String expectedMessage;

    public CreateUserTest(User user, String expectedMessage) {
        this.user = user;
        this.expectedMessage = expectedMessage;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {new User(null, "1234", "Alex"), "Email, password and name are required fields"},
                {new User("user-test120919913@yandex.ru", null, "Alex"), "Email, password and name are required fields"}
        });
    }

    @Test
    @Step("Можно создать уникального пользователя")
    public void testCreateValidUser() {
        User validUser = new User("user-test120919913@yandex.ru", "1234", "Alex");
        ValidatableResponse response = userService.createUser(validUser);

        // Проверка статуса и success
        response.assertThat()
                .statusCode(200)
                .body("success", is(true));

        // Проверка, что accessToken существует
        response.assertThat()
                .body("accessToken", notNullValue());

        // Извлекаем accessToken из ответа
        userAccessToken = response.extract().path("accessToken");
    }

    @Test
    @Step("Создание пользователя, который уже зарегистрирован")
    public void testCreateDuplicateUser() {
        User validUser = new User("user-test120919913@yandex.ru", "1234", "Alex");
        ValidatableResponse response = userService.createUser(validUser);

        // Проверка статуса и success
        response.assertThat()
                .statusCode(200)
                .body("success", is(true));

        // Проверка, что accessToken существует
        response.assertThat()
                .body("accessToken", notNullValue());

        // Извлекаем accessToken из ответа
        userAccessToken = response.extract().path("accessToken");

        //Создание пользователя, который уже зарегистрирован
        ValidatableResponse responseDuplicate = userService.createUser(validUser);
        responseDuplicate.assertThat()
                .statusCode(403)
                .and()
                .body("success", is(false),
                        "message", is("User already exists"));
    }

    @Test
    @Step("Создание пользователя без заполнения одного из обязательных полей")
    public void testMissingRequiredFields() {
        ValidatableResponse response = userService.createUser(user);

        // Проверка статуса и success
        response.assertThat()
                .statusCode(403)
                .and()
                .body("success", is(false),
                        "message", is(expectedMessage));
    }

    @After
    public void deleteUser() {
        if (userAccessToken != null && !userAccessToken.isEmpty()) { // Проверяем, есть ли accessToken
            userService.deleteUser(userAccessToken); // Удаление пользователя
        }
    }
}
