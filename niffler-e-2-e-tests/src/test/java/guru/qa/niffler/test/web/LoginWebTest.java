package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.open;

@ExtendWith(BrowserExtension.class)
public class LoginWebTest {

    private static final Config CFG = Config.getInstance();

    @Test
    void mainPageShouldBeDisplayedAfterSuccessLogin() {

        MainPage mainPage = open(CFG.frontUrl(), LoginPage.class)
                .login("cat", "123456");

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