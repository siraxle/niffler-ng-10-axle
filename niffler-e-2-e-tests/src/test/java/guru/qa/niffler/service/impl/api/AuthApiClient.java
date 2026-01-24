package guru.qa.niffler.service.impl.api;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.UserApi;
import guru.qa.niffler.api.core.CodeInterceptor;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.jupiter.extension.ApiLoginExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.RestClient;
import guru.qa.niffler.utils.OauthUtils;
import io.qameta.allure.Step;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@ParametersAreNonnullByDefault
public final class AuthApiClient extends RestClient {

    private final AuthApi authApi;
    private final UserApi userApi;
    private static final String RESPONSE_TYPE = "code";
    private static final String CLIENT_ID = "client";
    private static final String SCOPE = "openid";
    private static final String CODE_CHALLENGE_METHOD = "S256";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String REDIRECT_URL = CFG.frontUrl() + "authorized";

    public AuthApiClient() {
        super(CFG.authUrl(), true, new CodeInterceptor());
        this.authApi = create(AuthApi.class);

        Retrofit userdataRetrofit = new Retrofit.Builder()
                .baseUrl(CFG.userdataUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.userApi = userdataRetrofit.create(UserApi.class);
    }

    @Step("Регистрация пользователя: username={username}")
    @Nonnull
    public UserJson registerUser(String username, String password) throws IOException, InterruptedException {
        try {
            authApi.requestRegisterForm().execute();
        } catch (IOException e) {
            throw new IOException("Failed to get registration form", e);
        }

        Response<Void> registerResponse;
        try {
            registerResponse = authApi.register(
                    username,
                    password,
                    password,
                    ThreadSafeCookieStore.INSTANCE.xsrfCookie()
            ).execute();
        } catch (IOException e) {
            throw new IOException("Registration request failed", e);
        }

        if (!registerResponse.isSuccessful()) {
            throw new IOException("Registration failed with code: " + registerResponse.code());
        }

        try {
            return waitForUserCreation(username, Duration.ofSeconds(10));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedException("Registration wait interrupted");
        }
    }

    @Step("Ожидание создания пользователя: {username}")
    @Nonnull
    private UserJson waitForUserCreation(String username, Duration timeout)
            throws IOException, InterruptedException {
        long startTime = System.currentTimeMillis();
        long endTime = startTime + timeout.toMillis();

        while (System.currentTimeMillis() < endTime) {
            try {
                Response<UserJson> response = userApi.currentUser(username).execute();
                if (response.isSuccessful() && response.body() != null) {
                    UserJson userJson = response.body();
                    if (userJson.id() != null) {
                        return userJson;
                    }
                }
            } catch (IOException e) {
                // Игнорируем временные ошибки, продолжаем попытки
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }

        throw new IOException("User " + username + " was not created within " + timeout);
    }

    @Step("Простая регистрация (только запрос)")
    @Nonnull
    public Response<Void> register(String username, String password) throws IOException {
        try {
            authApi.requestRegisterForm().execute();
        } catch (IOException e) {
            throw new IOException("Failed to get registration form", e);
        }

        try {
            return authApi.register(
                    username,
                    password,
                    password,
                    ThreadSafeCookieStore.INSTANCE.xsrfCookie()
            ).execute();
        } catch (IOException e) {
            throw new IOException("Registration request failed", e);
        }
    }

    public void authorize(String codeChallenge) throws IOException {
        try {
            authApi.authorize(
                    RESPONSE_TYPE,
                    CLIENT_ID,
                    SCOPE,
                    REDIRECT_URL,
                    codeChallenge,
                    CODE_CHALLENGE_METHOD
            ).execute();
        } catch (IOException e) {
            throw new IOException("Authorize request failed", e);
        }
    }

    @SneakyThrows
    public String login(String username, String password) {
        final String codeVerifier = OauthUtils.generateCodeVerifier();
        final String codeChallenge = OauthUtils.generateCodeChallenge(codeVerifier);
        final String redirectUri = CFG.frontUrl() + "authorized";
        final String clientId = "client";

        authApi.authorize(
                "code",
                clientId,
                "openid",
                redirectUri,
                codeChallenge,
                "S256").execute();
        authApi.login(
                username,
                password,
                ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
        ).execute();

        Response<JsonNode> tokenResponse = authApi.token(
                ApiLoginExtension.getCode(),
                redirectUri,
                clientId,
                codeVerifier,
                GRANT_TYPE
        ).execute();
        return tokenResponse.body().get("id_token").asText();
    }


//    public Response<Void> register(String username, String password) throws IOException {
//        authApi.requestRegisterForm().execute();
//        return authApi.register(
//                username,
//                password,
//                password,
//                ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
//        ).execute();
//    }

    @SneakyThrows
    public String token(String code, String codeVerifier) {
        Response<JsonNode> response;
        try {
            response = authApi.token(
                    code,
                    REDIRECT_URL,
                    CLIENT_ID,
                    codeVerifier,
                    GRANT_TYPE
            ).execute();
        } catch (IOException e) {
            throw new IOException("Token request failed", e);
        }

        if (response.body() != null) {
            try {
                return response.body().path("id_token").asText();
            } catch (Exception e) {
                throw new IOException("Failed to extract id_token from response", e);
            }
        }

        throw new IOException("Token response body is null");
    }
}