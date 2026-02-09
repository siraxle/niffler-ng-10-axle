package guru.qa.niffler.test.rest;

import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.impl.api.AuthApiClient;
import guru.qa.niffler.service.impl.api.UsersApiClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationTest {

    private final AuthApiClient authApiClient = new AuthApiClient();

    @Test
    void shouldRegisterUserViaApi() throws IOException {
        String uniqueUsername = RandomDataUtils.randomUsername();

        Response<Void> response = authApiClient.register(uniqueUsername, "123456");
        assertEquals(201, response.code(),
                "Registration should return 201 Created. Response: " + response);
    }

    @Test
    public void testUserCreation() {
        UsersApiClient usersClient = new UsersApiClient();
        String username = "test_" + System.currentTimeMillis();

        try {
            UserJson user = usersClient.createUser(username, "123456");
            System.out.println("User created: " + user.username());
            System.out.println("User ID: " + user.id());

            Optional<UserJson> found = usersClient.findUserByUsername(username);
            System.out.println("User found: " + found.isPresent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}