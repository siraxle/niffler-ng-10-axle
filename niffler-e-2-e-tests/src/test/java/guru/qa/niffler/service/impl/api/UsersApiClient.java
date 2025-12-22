package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.AuthApi;
import guru.qa.niffler.api.UserApi;
import guru.qa.niffler.api.core.ThreadSafeCookieStore;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.model.TestData;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.UsersClient;
import lombok.NonNull;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.utils.RandomDataUtils.randomUsername;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public class UsersApiClient implements UsersClient {

    private static final Config CFG = Config.getInstance();
    public static final String DEFAULT_PASSWORD = "123456";

    private final Retrofit authRetrofit = new Retrofit.Builder()
            .baseUrl(CFG.authUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final Retrofit userdataRetrofit = new Retrofit.Builder()
            .baseUrl(CFG.userdataUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final AuthApi authApi = authRetrofit.create(AuthApi.class);
    private final UserApi userApi = userdataRetrofit.create(UserApi.class);

    @Override
    @NonNull
    public Optional<UserJson> findUserByUsername(String username) {
        try {
            Response<UserJson> response = userApi.currentUser(username).execute();
            if (response.isSuccessful()) {
                return Optional.ofNullable(response.body());
            } else if (response.code() == 404) {
                return Optional.empty();
            } else {
                throw new RuntimeException("Failed to find user. Code: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to find user", e);
        }
    }

    @Override
    @Nullable
    public UserJson createUser(String username, String password) {
        try {
            authApi.requestRegisterForm().execute();
            authApi.register(
                    username,
                    password,
                    password,
                    ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
            ).execute();
            UserJson createdUser = requireNonNull(userApi.currentUser(username).execute().body());
            return createdUser.addTestData(
                    new TestData(
                            password,
                            new ArrayList<>(), // incomeInvitations
                            new ArrayList<>(), // outcomeInvitations
                            new ArrayList<>(), // friends
                            new ArrayList<>(), // categories
                            new ArrayList<>()  // spendings
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @NonNull
    public List<UserJson> createFriends(UserJson targetUser, int count) {
        final List<UserJson> result = new ArrayList<>();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                final String username = randomUsername();
                final Response<UserJson> response;
                final UserJson newUser;
                try {
                    newUser = createUser(username, DEFAULT_PASSWORD);
                    result.add(newUser);

                    userApi.sendInvitation(
                            newUser.username(),
                            targetUser.username()
                    ).execute();
                    response = userApi.acceptInvitation(
                            targetUser.username(),
                            newUser.username()
                    ).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assertEquals(200, response.code(), "Accept invitation should return 200");
            }
        }
        return result;
    }

    @Override
    @NonNull
    public Optional<UserJson> findUserById(UUID id) {
        // получаем ВСЕХ пользователей и фильтруем
        try {
            List<UserJson> allUsers = userApi.allUsers("", "").execute().body();
            if (allUsers != null) {
                return allUsers.stream()
                        .filter(user -> user.id().equals(id))
                        .findFirst();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to find user by ID", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Delete user via REST API not implemented");
    }

    @Override
    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    @Override
    @NonNull
    public List<UserJson> addIncomeInvitation(UserJson targetUser, int count) {
        final List<UserJson> result = new ArrayList<>();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                final String username = randomUsername();
                final Response<UserJson> response;
                final UserJson newUser;
                try {
                    newUser = createUser(username, DEFAULT_PASSWORD);
                    result.add(newUser);
                    response = userApi.sendInvitation(
                            newUser.username(),
                            targetUser.username()
                    ).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assertEquals(200, response.code());
            }
        }
        return result;
    }

    @Override
    @NonNull
    public List<UserJson> addOutcomeInvitation(UserJson targetUser, int count) {
        final List<UserJson> result = new ArrayList<>();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                final String username = randomUsername();
                final Response<UserJson> response;
                final UserJson newUser;
                try {
                    newUser = createUser(username, DEFAULT_PASSWORD);
                    result.add(newUser);
                    response = userApi.sendInvitation(
                            targetUser.username(),
                            newUser.username()
                    ).execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                assertEquals(200, response.code());
            }
        }
        return result;
    }
}