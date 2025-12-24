package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class RegisterPage {

    private final SelenideElement logInLink = $("//a[@href='/login']");
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitPasswordInput = $("#passwordSubmit");
    private final SelenideElement signUpButton = $("#register-button");
    private final SelenideElement errorForm = $("*.form__error");

    @Step("Установить имя пользователя: {username}")
    @Nonnull
    public RegisterPage setUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    @Step("Установить пароль: {password}")
    @Nonnull
    public RegisterPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    @Step("Подтвердить пароль: {password}")
    @Nonnull
    public RegisterPage setSubmitPassword(String password) {
        submitPasswordInput.setValue(password);
        return this;
    }

    @Step("Нажать кнопку 'Sign Up'")
    public void clickSignUp() {
        signUpButton.click();
    }

    @Step("Перейти по ссылке 'Log In'")
    @Nonnull
    public LoginPage clickLogInLink() {
        logInLink.click();
        return new LoginPage();
    }

    @Step("Зарегистрировать пользователя с логином {username} и паролем {password}")
    @Nonnull
    public RegisterPage register(String username, String password) {
        setUsername(username)
                .setPassword(password)
                .setSubmitPassword(password)
                .clickSignUp();
        return this;
    }

    @Step("Проверить сообщение об ошибке: {expectedError}")
    public void checkError(String expectedError) {
        errorForm.shouldHave(text(expectedError));
    }

    @Step("Проверить, что страница регистрации содержит текст: {expectedTexts}")
    @Nonnull
    public RegisterPage checkThatRegisterPageContainsText(String... expectedTexts) {
        for (String text : expectedTexts) {
            $("body").shouldHave(text(text));
        }
        return this;
    }
}