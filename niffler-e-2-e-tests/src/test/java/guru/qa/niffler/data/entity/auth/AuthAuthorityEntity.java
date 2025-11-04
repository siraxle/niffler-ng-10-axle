package guru.qa.niffler.data.entity.auth;

import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class AuthAuthorityEntity implements Serializable {
    private UUID id;
    private UUID userId;
    private Authority authority;

    public AuthAuthorityEntity() {}

    public static AuthAuthorityEntity fromJson(AuthorityJson authorityJson) {
        AuthAuthorityEntity entity = new AuthAuthorityEntity();
        entity.setId(authorityJson.id());
        entity.setUserId(authorityJson.userId());
        entity.setAuthority(authorityJson.authority());
        return entity;
    }

}
