package guru.qa.niffler.test.web;

import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.NonStaticBrowsersExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.utils.SelenideUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import static com.codeborne.selenide.Condition.text;

@ExtendWith(NonStaticBrowsersExtension.class)
public class NonStaticBrowsersLoginTest {

    private static final Config CFG = Config.getInstance();

    @RegisterExtension
    static NonStaticBrowsersExtension browsers = new NonStaticBrowsersExtension();

    @User(username = "cat")
    @Test
    void successfulLoginInTwoBrowsers(UserJson user) {
        SelenideDriver chrome = new SelenideDriver(SelenideUtils.chromeConfig);
        SelenideDriver firefox = new SelenideDriver(SelenideUtils.firefoxConfig);

        browsers.addDriver(chrome);
        browsers.addDriver(firefox);

        LoginPage chromeLogin = new LoginPage(chrome);
        chrome.open(CFG.frontUrl(), LoginPage.class);
        chromeLogin.login(user.username(), user.testData().password());

        chrome.$("body").shouldHave(text("History of Spendings"));
        chrome.$("body").shouldHave(text("Statistics"));

        LoginPage firefoxLogin = new LoginPage(firefox);
        firefox.open(CFG.frontUrl(), LoginPage.class);
        firefoxLogin.login(user.username(), user.testData().password());

        firefox.$("body").shouldHave(text("History of Spendings"));
        firefox.$("body").shouldHave(text("Statistics"));
    }

    @Test
    void failedLoginInTwoBrowsers() {
        SelenideDriver chrome = new SelenideDriver(SelenideUtils.chromeConfig);
        SelenideDriver firefox = new SelenideDriver(SelenideUtils.firefoxConfig);

        browsers.addDriver(chrome);
        browsers.addDriver(firefox);

        LoginPage chromeLogin = new LoginPage(chrome);
        chrome.open(CFG.frontUrl(), LoginPage.class);
        chromeLogin.invalidLogin("nonexistentuser", "wrongpassword")
                .checkError("Неверные учетные данные пользователя");

        LoginPage firefoxLogin = new LoginPage(firefox);
        firefox.open(CFG.frontUrl(), LoginPage.class);
        firefoxLogin.invalidLogin("anotherwronguser", "anotherwrongpass")
                .checkError("Неверные учетные данные пользователя");
    }
}