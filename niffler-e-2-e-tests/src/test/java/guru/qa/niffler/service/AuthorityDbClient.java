package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static guru.qa.niffler.data.Databases.transaction;

public class AuthorityDbClient {
    private static final Config CFG = Config.getInstance();

    public AuthorityJson createAuthority(String username, String authority) {
        return transaction(connection -> {
            UUID userId = getUserIdByUsername(connection, username);

            AuthAuthorityEntity authAuthority = new AuthAuthorityEntity();
            authAuthority.setUserId(userId);
            authAuthority.setAuthority(Authority.valueOf(authority));

            AuthAuthorityEntity[] createdAuthorities = new AuthAuthorityDaoJdbc(connection).createAuthority(authAuthority);
            return AuthorityJson.fromEntity(createdAuthorities[0]);
        }, CFG.authJdbcUrl());
    }

    public AuthorityJson[] createAuthorities(String username, String... authorities) {
        return transaction(connection -> {
            UUID userId = getUserIdByUsername(connection, username);

            AuthAuthorityEntity[] authEntities = new AuthAuthorityEntity[authorities.length];
            for (int i = 0; i < authorities.length; i++) {
                AuthAuthorityEntity authAuthority = new AuthAuthorityEntity();
                authAuthority.setUserId(userId);
                authAuthority.setAuthority(Authority.valueOf(authorities[i]));
                authEntities[i] = authAuthority;
            }

            AuthAuthorityEntity[] createdAuthorities = new AuthAuthorityDaoJdbc(connection).createAuthority(authEntities);
            return toAuthorityJsonArray(createdAuthorities);
        }, CFG.authJdbcUrl());
    }

    public List<AuthorityJson> getAuthoritiesByUsername(String username) {
        return transaction(connection -> {
            UUID userId = getUserIdByUsername(connection, username);
            List<AuthAuthorityEntity> authorities = new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(userId);
            return authorities.stream()
                    .map(AuthorityJson::fromEntity)
                    .collect(Collectors.toList());
        }, CFG.authJdbcUrl());
    }

    public List<AuthorityJson> getAuthoritiesByUserId(UUID userId) {
        return transaction(connection -> {
            List<AuthAuthorityEntity> authorities = new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(userId);
            return authorities.stream()
                    .map(AuthorityJson::fromEntity)
                    .collect(Collectors.toList());
        }, CFG.authJdbcUrl());
    }

    public void deleteAuthority(String username, String authority) {
        transaction(connection -> {
            UUID userId = getUserIdByUsername(connection, username);
            List<AuthAuthorityEntity> userAuthorities = new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(userId);

            userAuthorities.stream()
                    .filter(auth -> auth.getAuthority().name().equals(authority))
                    .findFirst()
                    .ifPresent(auth -> new AuthAuthorityDaoJdbc(connection).deleteAuthority(auth));

            return null;
        }, CFG.authJdbcUrl());
    }

    public void deleteAllAuthorities(String username) {
        transaction(connection -> {
            UUID userId = getUserIdByUsername(connection, username);
            List<AuthAuthorityEntity> authorities = new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(userId);

            for (AuthAuthorityEntity authority : authorities) {
                new AuthAuthorityDaoJdbc(connection).deleteAuthority(authority);
            }

            return null;
        }, CFG.authJdbcUrl());
    }

    private UUID getUserIdByUsername(Connection connection, String username) {
        Optional<AuthUserEntity> user = new AuthUserDaoJdbc(connection).findByUsername(username);
        return user.map(AuthUserEntity::getId)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    private AuthorityJson[] toAuthorityJsonArray(AuthAuthorityEntity[] entities) {
        AuthorityJson[] result = new AuthorityJson[entities.length];
        for (int i = 0; i < entities.length; i++) {
            result[i] = AuthorityJson.fromEntity(entities[i]);
        }
        return result;
    }
}