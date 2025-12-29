package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.UserApi;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.RestClient;
import io.qameta.allure.Step;
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

    public AuthApiClient() {
        super("https://auth.niffler-stage.qa.guru/", true);
        this.authApi = create(AuthApi.class);

        Retrofit userdataRetrofit = new Retrofit.Builder()
                .baseUrl("https://userdata.niffler-stage.qa.guru/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        this.userApi = userdataRetrofit.create(UserApi.class);
    }

    @Step("Регистрация пользователя: username={username}")
    @Nonnull
    public UserJson registerUser(String username, String password) throws IOException, InterruptedException {
        authApi.requestRegisterForm().execute();

        Response<Void> registerResponse = authApi.register(
                username,
                password,
                password,
                ThreadSafeCookieStore.INSTANCE.xsrfCookie()
        ).execute();

        if (!registerResponse.isSuccessful()) {
            throw new RuntimeException("Registration failed with code: " + registerResponse.code());
        }
        return waitForUserCreation(username, Duration.ofSeconds(10));
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

        throw new RuntimeException("User " + username + " was not created within " + timeout);
    }

    @Step("Простая регистрация (только запрос)")
    @Nonnull
    public Response<Void> register(String username, String password) throws IOException {
        authApi.requestRegisterForm().execute();
        return authApi.register(
                username,
                password,
                password,
                ThreadSafeCookieStore.INSTANCE.xsrfCookie()
        ).execute();
    }
}