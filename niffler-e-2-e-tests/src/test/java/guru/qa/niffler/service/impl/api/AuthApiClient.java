package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.UserApi;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.RestClient;
import io.qameta.allure.Step;
import org.apache.commons.lang3.StringUtils;
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
        super(CFG.authUrl(), true);
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

    public String login(String username, String password) throws IOException {
        Response<Void> response;
        try {
            response = authApi.login(
                    username,
                    password,
                    ThreadSafeCookieStore.INSTANCE.xsrfCookie()
            ).execute();
        } catch (IOException e) {
            throw new IOException("Login request failed", e);
        }

        try {
            return StringUtils.substringAfter(response.raw().request().url().toString(), "code=");
        } catch (Exception e) {
            throw new IOException("Failed to extract code from response", e);
        }
    }

    public String token(String code, String codeVerifier) throws IOException {
        Response<com.fasterxml.jackson.databind.JsonNode> response;
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