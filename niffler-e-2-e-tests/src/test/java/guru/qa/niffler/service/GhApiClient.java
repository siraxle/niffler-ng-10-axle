package guru.qa.niffler.service;

import com.fasterxml.jackson.databind.JsonNode;
import guru.qa.niffler.api.GhApi;
import guru.qa.niffler.config.Config;
import lombok.SneakyThrows;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Objects;


public class GhApiClient {

    private static final Config CFG = Config.getInstance();
    private static final String GH_TOKEN_ENV = "GITHUB_TOKEN";

    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(CFG.ghUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final GhApi ghApi = retrofit.create(GhApi.class);

    @SneakyThrows
    public String issueState(String issueNumber) {


        JsonNode response = ghApi.issue(
                "Bearer " + System.getenv(GH_TOKEN_ENV),
//                "Bearer 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",
                issueNumber
        ).execute().body();

        return Objects.requireNonNull(response).get("state").asText();
    }
}
