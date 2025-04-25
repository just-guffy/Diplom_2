import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import io.restassured.response.ValidatableResponse;
import io.qameta.allure.Step;
import model.User;
import model.Credentials;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import user.UserService;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class LoginUserTest {

    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";

    private User user;
    private UserService userService;
    private String userAccessToken;

    private final String email;
    private final String password;

    public LoginUserTest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @Before
    public void setUp() {
        userService = new UserService(BASE_URI);
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
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"1user-test120919913@yandex.ru", "1234"}, // Неправильный email + правильный пароль
                {"user-test120919913@yandex.ru", "4321"},  // Правильный email + неправильный пароль
                {null, "1234"},                            // Пустой email + правильный пароль
                {"user-test120919913@yandex.ru", null}     // Правильный email + пустой пароль
        });
    }

    @Test
    @Step("Логин под существующим пользователем")
    public void login_ok() {
        Credentials credentials = Credentials.fromUser(user);
        ValidatableResponse response = userService.login(credentials);

        // Проверка статуса и success
        response.assertThat()
                .statusCode(200)
                .body("success", is(true));
    }

    @Test
    @Step("Проверка авторизации с невалидными данными")
    public void login_invalidCredentials() {
        Credentials credentials = new Credentials(email, password);
        ValidatableResponse response = userService.login(credentials);

        // Проверка статуса, success и message
        response.assertThat()
                .statusCode(401)
                .body("success", is(false),
                        "message", is("email or password are incorrect"));
    }

    @After
    public void deleteUser() {
        if (userAccessToken != null && !userAccessToken.isEmpty()) { // Проверяем, есть ли accessToken
            userService.deleteUser(userAccessToken); // Удаление пользователя
        }
    }
}
