package guru.qa.niffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;

import javax.annotation.Nonnull;
import java.util.UUID;

public record AuthorityJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("user_id")
        UUID userId,
        @JsonProperty("authority")
        Authority authority) {

    public static @Nonnull AuthorityJson fromEntity(@Nonnull AuthorityEntity entity) {
        return new AuthorityJson(
                entity.getId(),
                entity.getUser().getId(),
                entity.getAuthority()
        );
    }

    public static @Nonnull AuthorityJson[] toAuthorityJsonArray(@Nonnull AuthorityEntity[] entities) {
        AuthorityJson[] result = new AuthorityJson[entities.length];
        for (int i = 0; i < entities.length; i++) {
            result[i] = AuthorityJson.fromEntity(entities[i]);
        }
        return result;
    }

}