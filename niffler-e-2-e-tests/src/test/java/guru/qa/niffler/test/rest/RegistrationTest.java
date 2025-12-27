package guru.qa.niffler.test.rest;

import guru.qa.niffler.service.impl.api.AuthApiClient;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import java.io.IOException;

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

}