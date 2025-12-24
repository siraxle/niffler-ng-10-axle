package guru.qa.niffler.service.impl.api;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.GhAPI;
import guru.qa.niffler.service.RestClient;
import retrofit2.Response;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ParametersAreNonnullByDefault
public final class GhAPIClient extends RestClient {

    private static final String GH_TOKEN_ENV = "GITHUB_TOKEN";

    private final GhAPI ghApi;

    public GhAPIClient() {
        super(CFG.ghUrl());
        this.ghApi = create(GhAPI.class);
    }

    public @Nonnull String issueState(String issueNumber) {
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
