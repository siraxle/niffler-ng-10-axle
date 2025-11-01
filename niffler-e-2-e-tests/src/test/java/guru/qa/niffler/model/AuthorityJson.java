package guru.qa.niffler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;

import java.util.UUID;

public record AuthorityJson(
        @JsonProperty("id")
        UUID id,
        @JsonProperty("user_id")
        UUID userId,
        @JsonProperty("authority")
        Authority authority) {

    public static AuthorityJson fromEntity(AuthAuthorityEntity entity) {
        return new AuthorityJson(
                entity.getId(),
                entity.getUserId(),
                entity.getAuthority()
        );
    }
}