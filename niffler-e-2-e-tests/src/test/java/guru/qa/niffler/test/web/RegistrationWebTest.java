package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.page.RegisterPage;
import guru.qa.niffler.service.impl.api.AuthApiClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import retrofit2.Response;

import java.io.IOException;

@ExtendWith(BrowserExtension.class)
public class RegistrationWebTest {

    private static final Config CFG = Config.getInstance();
    private final AuthApiClient authApiClient = new AuthApiClient();

    @Test
    void shouldRegisterNewUser() {
        final String randomUsername = RandomDataUtils.randomUsername();

        Selenide.open(CFG.registerUrl(), RegisterPage.class)
                .register(randomUsername, "123456")
                .checkThatRegisterPageContainsText("Congratulations! You've registered!");
    }

    @Test
    void shouldNotRegisterUserWithExistingUserName() {
        RegisterPage registerPage = Selenide.open(CFG.registerUrl(), RegisterPage.class);
        registerPage
                .setUsername("cat")
                .setPassword("123456")
                .setSubmitPassword("123456")
                .clickSignUp();

        registerPage.checkError("Username `cat` already exists");
    }

    @Test
    void shouldShowErrorIfPasswordAndConfirmPasswordAreNotEqual() {
        RegisterPage registerPage = Selenide.open(CFG.registerUrl(), RegisterPage.class);
        registerPage
                .setUsername("user_" + System.currentTimeMillis())
                .setPassword("12345")
                .setSubmitPassword("different")
                .clickSignUp();

        registerPage.checkError("Passwords should be equal");
    }


    @Test
    void newUserShouldRegisterByApiCall() throws IOException {
        final String randomUsername = RandomDataUtils.randomUsername();

        final Response<Void> response = authApiClient.register(randomUsername, "123456");
        Assertions.assertEquals(201, response.code());
    }

}
