package guru.qa.niffler.jupiter.extension;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.model.*;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.service.SpendClient;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.impl.api.AuthApiClient;
import guru.qa.niffler.service.impl.api.SpendApiClient;
import guru.qa.niffler.service.impl.api.UsersApiClient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import org.openqa.selenium.Cookie;

import java.util.List;
import java.util.Optional;

public class ApiLoginExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApiLoginExtension.class);
    private static final Config CFG = Config.getInstance();
    private final boolean setupBrowser;

    private final AuthApiClient authApiClient = new AuthApiClient();
    private final UsersClient usersClient = new UsersApiClient();
    private final SpendClient spendClient = new SpendApiClient();

    private ApiLoginExtension(boolean setupBrowser) {
        this.setupBrowser = setupBrowser;
    }

    public ApiLoginExtension() {
        this.setupBrowser = true;
    }

    public static ApiLoginExtension rest() {
        return new ApiLoginExtension(false);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), ApiLogin.class)
                .ifPresent(apiLogin -> {
                    final UserJson userToLogin;
                    final Optional<UserJson> userFromUserExtension = UserExtension.getUserJson();

                    if (!apiLogin.username().isEmpty() && !apiLogin.password().isEmpty()) {
                        String username = apiLogin.username();
                        String password = apiLogin.password();

                        List<CategoryJson> categories = spendClient.getAllCategories(username);
                        List<SpendJson> spends = spendClient.allSpends(username);
                        List<UserJson> friendsList = usersClient.getFriends(username);

                        List<UserJson> friends = friendsList.stream()
                                .filter(f -> f.friendshipStatus() != null &&
                                        f.friendshipStatus().equals(FriendshipStatus.FRIEND))
                                .toList();

                        List<UserJson> incomeInvitations = friendsList.stream()
                                .filter(f -> f.friendshipStatus() != null &&
                                        f.friendshipStatus().equals(FriendshipStatus.INVITE_RECEIVED))
                                .toList();

                        List<UserJson> outcomeInvitations = usersClient.allUsers(username).stream()
                                .filter(f -> f.friendshipStatus() != null &&
                                        f.friendshipStatus().equals(FriendshipStatus.INVITE_SENT))
                                .toList();

                        TestData testData = new TestData(
                                password,
                                incomeInvitations,
                                outcomeInvitations,
                                friends,
                                categories,
                                spends
                        );

                        UserJson existingUser = new UserJson(
                                username,
                                testData
                        );

                        userToLogin = existingUser;

                    } else if (apiLogin.username().isEmpty() && apiLogin.password().isEmpty()) {
                        if (userFromUserExtension.isEmpty()) {
                            throw new IllegalStateException("@User must be present in case that @ApiLogin is empty!");
                        }
                        userToLogin = userFromUserExtension.get();
                    } else {
                        throw new IllegalStateException("Both username and password must be provided in @ApiLogin or both must be empty!");
                    }

                    final String token = authApiClient.login(
                            userToLogin.username(),
                            userToLogin.testData().password()
                    );
                    setToken(token);

                    if (setupBrowser) {
                        Selenide.open(CFG.frontUrl());
                        Selenide.localStorage().setItem("id_token", getToken());
                        WebDriverRunner.getWebDriver().manage().addCookie(
                                new Cookie(
                                        "JSESSIONID",
                                        ThreadSafeCookieStore.INSTANCE.cookieValue("JSESSIONID")
                                )
                        );
                        Selenide.open(MainPage.URL, MainPage.class);
                    }
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(String.class)
                && AnnotationSupport.isAnnotated(parameterContext.getParameter(), Token.class);
    }

    @Override
    public String resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return "Bearer " + getToken();
    }

    @Override
    public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
        return ParameterResolver.super.getTestInstantiationExtensionContextScope(rootContext);
    }

    public static void setToken(String token) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("token", token);
    }


    public static String getToken() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("token", String.class);
    }

    public static void setCode(String code) {
        TestMethodContextExtension.context().getStore(NAMESPACE).put("code", code);
    }

    public static String getCode() {
        return TestMethodContextExtension.context().getStore(NAMESPACE).get("code", String.class);
    }

    public static Cookie getJsessionIdCookie() {
        return new Cookie(
                "JSESSIONID",
                ThreadSafeCookieStore.INSTANCE.cookieValue("JSESSIONID")
        );
    }
}