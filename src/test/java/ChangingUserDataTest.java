import io.restassured.response.ValidatableResponse;
import io.qameta.allure.Step;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import user.UserService;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class ChangingUserDataTest {

    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";
    private UserService userService;
    private String userAccessToken;
    private User originalUser;

    private final String fieldToUpdate;
    private final String newValue;

    public ChangingUserDataTest(String fieldToUpdate, String newValue) {
        this.fieldToUpdate = fieldToUpdate;
        this.newValue = newValue;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"email", "updated-user-test120919913@yandex.ru"},
                {"name", "Denis"},
                {"password", "newpassword4321"}
        });
    }

    @Before
    public void setUp() {
        userService = new UserService(BASE_URI);
        originalUser = new User("user-test120919913@yandex.ru", "1234", "Alex");

        // Создание пользователя
        ValidatableResponse createResponse = userService.createUser(originalUser);
        createResponse.assertThat()
                .statusCode(200)
                .body("success", is(true));

        // Проверка, что accessToken существует
        createResponse.assertThat()
                .body("accessToken", notNullValue());

        // Получение токена
        userAccessToken = createResponse.extract().path("accessToken");
    }

    private User createUpdatedUser() {
        switch (fieldToUpdate) {
            case "email":
                return new User(newValue, originalUser.getPassword(), originalUser.getName());
            case "name":
                return new User(originalUser.getEmail(), originalUser.getPassword(), newValue);
            case "password":
                return new User(originalUser.getEmail(), newValue, originalUser.getName());
            default:
                return originalUser;
        }
    }

    @Test
    @Step("Обновление данных пользователя с авторизацией")
    public void shouldUpdateUserWithAuthorization() {
        User updatedUser = createUpdatedUser();

        ValidatableResponse updateResponse = userService.updateUserInfo(userAccessToken, updatedUser);

        updateResponse.assertThat()
                .statusCode(200)
                .body(
                        "success", is(true),
                        "user.email", equalTo(updatedUser.getEmail()),
                        "user.name", equalTo(updatedUser.getName())
                );

        // Проверка, что данные действительно изменились
        ValidatableResponse getResponse = userService.getUserInfo(userAccessToken);
        getResponse.assertThat()
                .statusCode(200)
                .body(
                        "success", is(true),
                        "user.email", equalTo(updatedUser.getEmail()),
                        "user.name", equalTo(updatedUser.getName())
                );
    }

    @Test
    @Step("Обновление данных пользователя без авторизации")
    public void shouldNotUpdateUserWithoutAuthorization() {
        User updatedUser = createUpdatedUser();

        ValidatableResponse updateResponse = userService.updateUserInfoUnauthorized(updatedUser);

        // Проверка статуса, success и message
        updateResponse.assertThat()
                .statusCode(401)
                .body("success", is(false),
                        "message", is("You should be authorised"));
    }

    @After
    public void deleteUser() {
        if (userAccessToken != null && !userAccessToken.isEmpty()) { // Проверяем, есть ли accessToken
            userService.deleteUser(userAccessToken); // Удаление пользователя
        }
    }
}