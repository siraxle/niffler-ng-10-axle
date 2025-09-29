package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.page.RegisterPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(BrowserExtension.class)
public class RegistrationWebTest {

    private static final Config CFG = Config.getInstance();

    @Test
    void shouldRegisterNewUser() {
        Selenide.open(CFG.registerUrl(), RegisterPage.class)
                .register("newuser_" + System.currentTimeMillis(), "12345")
                .checkThatRegisterPageContainsText("Congratulations! You've registered!");
    }

    @Test
    void shouldNotRegisterUserWithExistingUserName() {
        RegisterPage registerPage = new RegisterPage();
        Selenide.open(CFG.registerUrl(), RegisterPage.class)
                .setUsername("cat")
                .setPassword("123456")
                .setSubmitPassword("123456")
                .clickSignUp();

        registerPage.checkError("Username `cat` already exists");
    }

    @Test
    void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        RegisterPage registerPage = new RegisterPage();
        Selenide.open(CFG.registerUrl(), RegisterPage.class)
                .setUsername("user_" + System.currentTimeMillis())
                .setPassword("12345")
                .setSubmitPassword("different")
                .clickSignUp();

        registerPage.checkError("Passwords should be equal");
    }
}
