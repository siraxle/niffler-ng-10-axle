package guru.qa.niffler.test.web;

//import com.codeborne.selenide.Selenide;

import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UserExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.utils.SelenideUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.open;

@ExtendWith({BrowserExtension.class, UserExtension.class})
public class LoginWebTest {

    private static final Config CFG = Config.getInstance();

    @RegisterExtension
    private final BrowserExtension browserExtension = new BrowserExtension();
    private final SelenideDriver driver = new SelenideDriver(SelenideUtils.chromeConfig);

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
        SelenideDriver firefox = new  SelenideDriver(SelenideUtils.firefoxConfig);
        browserExtension.getDrivers().addAll(List.of(driver, firefox));
        LoginPage loginPage = new LoginPage(driver);
        driver.open(CFG.frontUrl(), LoginPage.class);
        loginPage.invalidLogin("nonexistentuser", "wrongpassword")
                .checkError("Неверные учетные данные пользователя");
    }

    @User()
    @Test
    void mainPageShouldBeDisplayedAfterSuccessLogin2(UserJson user) {
        browserExtension.getDrivers().add(driver);
        driver.open(LoginPage.URL);
        MainPage mainPage = new LoginPage(driver).login(user.username(), user.testData().password());

        mainPage.checkThatMainPageContainsText("History of Spendings");
        mainPage.checkThatMainPageContainsText("Statistics");
    }

    @Test
    void parallelWrongLoginTestInTwoBrowsers() {
        SelenideDriver chromeDriver = new SelenideDriver(SelenideUtils.chromeConfig);
        SelenideDriver firefoxDriver = new SelenideDriver(SelenideUtils.firefoxConfig);

        browserExtension.getDrivers().addAll(List.of(chromeDriver, firefoxDriver));

        LoginPage chromeLogin = new LoginPage(chromeDriver);
        chromeDriver.open(CFG.frontUrl(), LoginPage.class);

        LoginPage firefoxLogin = new LoginPage(firefoxDriver);
        firefoxDriver.open(CFG.frontUrl(), LoginPage.class);

        chromeLogin.invalidLogin("user1", "pass1").checkError("Неверные учетные данные пользователя");
        firefoxLogin.invalidLogin("user2", "pass2").checkError("Неверные учетные данные пользователя");
    }

    @User(username = "cat")
    @Test
    void loginWithSameUserInTwoBrowsers2(UserJson user) {
        SelenideDriver chrome = new SelenideDriver(SelenideUtils.chromeConfig);
        SelenideDriver firefox = new SelenideDriver(SelenideUtils.firefoxConfig);
        browserExtension.getDrivers().addAll(List.of(chrome, firefox));

        // Chrome
        LoginPage chromeLogin = new LoginPage(chrome);
        chrome.open(CFG.frontUrl(), LoginPage.class);
        chromeLogin.login(user.username(), user.testData().password());
        chrome.$("body").shouldHave(text("History of Spendings"));
        chrome.$("body").shouldHave(text("Statistics"));

        // Firefox
        LoginPage firefoxLogin = new LoginPage(firefox);
        firefox.open(CFG.frontUrl(), LoginPage.class);
        firefoxLogin.login(user.username(), user.testData().password());
        firefox.$("body").shouldHave(text("History of Spendings"));
        firefox.$("body").shouldHave(text("Statistics"));

    }

}