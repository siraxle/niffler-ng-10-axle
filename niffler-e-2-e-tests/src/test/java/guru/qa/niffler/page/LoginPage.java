package guru.qa.niffler.page;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideDriver;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;

@ParametersAreNonnullByDefault
public class LoginPage extends BasePage<LoginPage> {

    public static final String URL = CFG.authUrl() + "login";

    private final SelenideElement usernameInput;
    private final SelenideElement passwordInput;
    private final SelenideElement submitBtn;
    private final SelenideElement createNewAccountBtn;
    private final SelenideElement errorForm;

    public LoginPage(SelenideDriver driver) {
        super(driver);
        this.usernameInput = driver.$("#username");
        this.passwordInput = driver.$("#password");
        this.submitBtn = driver.$("#login-button");
        this.createNewAccountBtn = driver.$("#register-button");
        this.errorForm = driver.$("*.form__error");
    }

    public LoginPage() {
        this.usernameInput = Selenide.$("#username");
        this.passwordInput = Selenide.$("#password");
        this.submitBtn = Selenide.$("#login-button");
        this.createNewAccountBtn = Selenide.$("#register-button");
        this.errorForm = Selenide.$("*.form__error");
    }

    @Step("Выполнить вход с логином {username} и паролем {password}")
    @Nonnull
    public MainPage login(String username, String password) {
        usernameInput.val(username);
        passwordInput.val(password);
        submitBtn.click();
        return new MainPage();
    }

    @Step("Попытка невалидного входа с логином {username} и паролем {password}")
    @Nonnull
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