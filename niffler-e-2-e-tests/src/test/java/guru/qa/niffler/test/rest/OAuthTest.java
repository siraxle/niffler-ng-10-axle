package guru.qa.niffler.test.rest;


import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.Token;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.FriendsPage;
import guru.qa.niffler.service.impl.api.AuthApiClient;
import guru.qa.niffler.utils.OauthUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OAuthTest {

    private final AuthApiClient authApiClient = new AuthApiClient();
    private static final Config CFG = Config.getInstance();

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

    @Test
    @User()
    @ApiLogin()
    public void testAuth1(@Token String token, UserJson user){
        System.out.println(user);
        System.out.println(token);
        Assertions.assertNotNull(token);
    }

    @Test
    @ApiLogin(username = "cat", password = "123456")
    public void testAuth2(@Token String token, UserJson user){
        System.out.println(user);
        System.out.println(token);
        Assertions.assertNotNull(token);
    }

    @Test
    @User(friends = 1)
    @ApiLogin
    public void testAuth3(@Token String token, UserJson user){
        System.out.println(user);
        System.out.println(token);
        Assertions.assertNotNull(token);
    }

    @Test
    @User(username = "cat")
    @ApiLogin()
    public void testAuth4(@Token String token, UserJson user){
        System.out.println(user);
        System.out.println(token);
        Assertions.assertNotNull(token);
    }

    @Test
    @ApiLogin(username = "cat", password = "123456")
    void testWithExistingUserAndFriends(UserJson user) {
        System.out.println("User: " + user.username());
        System.out.println("Categories: " + user.testData().categories().size());
        System.out.println("Spendings: " + user.testData().spendings().size());
        System.out.println("Friends: " + user.testData().friends().size());
        System.out.println("Income invitations: " + user.testData().incomeInvitations().size());
        System.out.println("Outcome invitations: " + user.testData().outcomeInvitations().size());

        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .checkFriendsCount(user.testData().friends().size());
    }

}