package guru.qa.niffler.service.impl.api;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.GhAPI;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.model.SpendJson;
import lombok.NonNull;
import lombok.SneakyThrows;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public class GhAPIClient {

    private static final Config CFG = Config.getInstance();
    private static final String GH_TOKEN_ENV = "GITHUB_TOKEN";

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(CFG.ghUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final GhAPI ghApi = retrofit.create(GhAPI.class);

    public @NonNull String issueState(String issueNumber) {
        final Response<JsonNode> response;
        try {
            response = ghApi.issue(
                    "Bearer " + System.getenv(GH_TOKEN_ENV),
                    issueNumber
            ).execute();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        assertEquals(200, response.code());
        return Objects.requireNonNull(response.body()).get("state").asText();
    }
}
