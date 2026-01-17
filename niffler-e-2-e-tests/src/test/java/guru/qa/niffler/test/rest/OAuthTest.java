package guru.qa.niffler.test.rest;

import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.impl.api.AuthApiClient;
import guru.qa.niffler.utils.OauthUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OAuthTest {

    private final AuthApiClient authApiClient = new AuthApiClient();

    @Test
    @User(username = "test1")
    public void oauthTest(UserJson user) throws IOException {
        String codeVerifier = OauthUtils.generateCodeVerifier();
        String codeChallenge = OauthUtils.generateCodeChallenge(codeVerifier);
        authApiClient.authorize(codeChallenge);
        String code = authApiClient.login(user.username(), user.testData().password());
        System.out.println(code);
        String token = authApiClient.token(code, codeVerifier);
        assertNotNull(token);
        System.out.println("Final Token: " + token);
    }

}