package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;

import java.util.List;
import java.util.UUID;

public interface AuthAuthorityDao {
    AuthAuthorityEntity[] createAuthority(AuthAuthorityEntity... authority);

    List<AuthAuthorityEntity> findAuthoritiesByUserId(UUID userId);

    void deleteAuthority(AuthAuthorityEntity authority);
}
