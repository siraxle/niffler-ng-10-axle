package guru.qa.niffler.page;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class RegisterPage {

    private final SelenideElement logInLink = $("//a[@href='/login']");
    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitPasswordInput = $("#passwordSubmit");
    private final SelenideElement signUpButton = $("#register-button");

    public RegisterPage setUsername(String username) {
        usernameInput.setValue(username);
        return this;
    }

    public RegisterPage setPassword(String password) {
        passwordInput.setValue(password);
        return this;
    }

    public RegisterPage setSubmitPassword(String password) {
        submitPasswordInput.setValue(password);
        return this;
    }

    public void clickSignUp() {
        signUpButton.click();
    }

    public LoginPage clickLogInLink() {
        logInLink.click();
        return new LoginPage();
    }

    public RegisterPage register(String username, String password) {
        setUsername(username)
        .setPassword(password)
        .setSubmitPassword(password)
        .clickSignUp();
        return this;
    }

    public void checkError(String expectedError) {
        $("*.form__error").shouldHave(text(expectedError));
    }

    public RegisterPage checkThatRegisterPageContainsText(String... expectedTexts) {
        for (String text : expectedTexts) {
            $("body").shouldHave(text(text));
        }
        return this;
    }
}
