package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UserExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.open;

@ExtendWith({BrowserExtension.class, UserExtension.class})
public class LoginWebTest {

    private static final Config CFG = Config.getInstance();

    @User()
    @Test
    void mainPageShouldBeDisplayedAfterSuccessLogin(UserJson user) {
        MainPage mainPage = open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());

        mainPage.checkThatMainPageContainsText("History of Spendings");
        mainPage.checkThatMainPageContainsText("Statistics");
    }

    @User(
            username = "cat"
    )
    @Test
    void mainPageShouldBeDisplayedAfterSuccessLogin1(UserJson user) {
        MainPage mainPage = open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());

        mainPage.checkThatMainPageContainsText("History of Spendings");
        mainPage.checkThatMainPageContainsText("Statistics");
    }

    @Test
    void userShouldStayOnLoginPageAfterLoginWithBadCredentials() {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .invalidLogin("nonexistentuser", "wrongpassword")
                .checkError("Неверные учетные данные пользователя");
    }
}