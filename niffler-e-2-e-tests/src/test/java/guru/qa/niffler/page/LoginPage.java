package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage {
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitBtn = $("#login-button");
    private final SelenideElement createNewAccountBtn = $("#register-button");
    private final SelenideElement errorForm = $("*.form__error");

    @Step("Выполнить вход с логином {username} и паролем {password}")
    public MainPage login(String username, String password) {
        usernameInput.val(username);
        passwordInput.val(password);
        submitBtn.click();
        return new MainPage();
    }

    @Step("Попытка невалидного входа с логином {username} и паролем {password}")
    public LoginPage invalidLogin(String username, String password) {
        usernameInput.val(username);
        passwordInput.val(password);
        submitBtn.click();
        return this;
    }

    @Step("Проверить сообщение об ошибке: {expectedError}")
    public void checkError(String expectedError) {
        errorForm.shouldHave(text(expectedError));
    }
}