package guru.qa.niffler.service.impl.api;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.GhAPI;
import guru.qa.niffler.config.Config;
import lombok.SneakyThrows;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Objects;


public class GhAPIClient {

    private static final Config CFG = Config.getInstance();
    private static final String GH_TOKEN_ENV = "GITHUB_TOKEN";

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(CFG.ghUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final GhAPI ghApi = retrofit.create(GhAPI.class);

    @SneakyThrows
    public String issueState(String issueNumber) {

        JsonNode response = ghApi.issue(
                "Bearer " + System.getenv(GH_TOKEN_ENV),
                issueNumber
        ).execute().body();

        return Objects.requireNonNull(response).get("state").asText();
    }
}
