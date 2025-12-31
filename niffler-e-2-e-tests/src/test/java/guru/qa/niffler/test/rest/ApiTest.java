package guru.qa.niffler.test.rest;

import guru.qa.niffler.api.UserApi;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.model.UserJson;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Isolated
public class ApiTest {

    private static UserApi userApi;
    private static final String EXISTING_USER = "cat";
    private static final String NON_EXISTING_SEARCH = "NON_EXISTING_SEARCH_QUERY";

    @BeforeAll
    static void init() {
        Config cfg = Config.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(cfg.userdataUrl())
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        userApi = retrofit.create(UserApi.class);
    }

    @Test
    @Order(1)
    @DisplayName("GET /internal/users/all возвращает непустой список для существующего пользователя")
    void allUsers_shouldReturnNonEmptyList() throws IOException {
        Response<List<UserJson>> response = userApi.allUsers(EXISTING_USER, null).execute();

        assertTrue(response.isSuccessful());
        assertEquals(200, response.code());

        List<UserJson> users = response.body();
        assertNotNull(users);
    }

    @Test
    @Order(2)
    @DisplayName("GET /internal/users/all возвращает пустой список при поиске несуществующего пользователя")
    void allUsers_shouldReturnEmptyList_whenNoMatches() throws IOException {
        Response<List<UserJson>> response = userApi.allUsers(EXISTING_USER, NON_EXISTING_SEARCH).execute();

        assertTrue(response.isSuccessful());
        assertEquals(200, response.code());

        List<UserJson> users = response.body();
        assertNotNull(users);
    }
}