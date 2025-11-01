package guru.qa.niffler.data.entity.auth;

import guru.qa.niffler.model.Authority;
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
}
