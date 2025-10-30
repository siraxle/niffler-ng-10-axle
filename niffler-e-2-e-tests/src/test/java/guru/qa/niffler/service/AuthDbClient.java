package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.UserAuthorityDaoJdbc;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;
import guru.qa.niffler.model.UserAuthJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static guru.qa.niffler.data.Databases.transaction;

public class AuthDbClient {
    private static final Config CFG = Config.getInstance();

    private AuthUserEntity fromJson(UserAuthJson user) {
        AuthUserEntity userEntity = new AuthUserEntity();
        userEntity.setUsername(user.username());
        userEntity.setPassword(user.password());
        userEntity.setEnabled(user.enabled());
        userEntity.setAccountNonExpired(user.accountNonExpired());
        userEntity.setAccountNonLocked(user.accountNonLocked());
        userEntity.setCredentialsNonExpired(user.credentialsNonExpired());
        return userEntity;
    }

    private AuthUserEntity toAuthUserEntityWithId(UserAuthJson user) {
        AuthUserEntity userEntity = fromJson(user);
        userEntity.setId(user.id());
        return userEntity;
    }

    public UserAuthJson createUser(UserAuthJson user, String... authorities) {
        return transaction(connection -> {
            AuthUserEntity createdUser = new UserAuthorityDaoJdbc(connection).createUser(fromJson(user));

            if (authorities != null && authorities.length > 0) {
                for (String authority : authorities) {
                    AuthAuthorityEntity authAuthority = new AuthAuthorityEntity();
                    authAuthority.setUserId(createdUser.getId());
                    authAuthority.setAuthority(Authority.valueOf(authority));
                    new UserAuthorityDaoJdbc(connection).createAuthority(authAuthority);
                }
            }

            return UserAuthJson.fromEntity(createdUser);
        }, CFG.authJdbcUrl());
    }

    public Optional<UserAuthJson> findUserByUsername(String username) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new UserAuthorityDaoJdbc(connection).findUserByUsername(username);
            return user.map(UserAuthJson::fromEntity);
        }, CFG.authJdbcUrl());
    }

    public Optional<UserAuthJson> findUserById(UUID id) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new UserAuthorityDaoJdbc(connection).findUserById(id);
            return user.map(UserAuthJson::fromEntity);
        }, CFG.authJdbcUrl());
    }

    public List<AuthorityJson> getUserAuthorities(String username) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new UserAuthorityDaoJdbc(connection).findUserByUsername(username);

            if (user.isPresent()) {
                List<AuthAuthorityEntity> authorities =
                        new UserAuthorityDaoJdbc(connection).findAuthoritiesByUserId(user.get().getId());

                return authorities.stream()
                        .map(AuthorityJson::fromEntity)
                        .collect(Collectors.toList());
            } else {
                return List.of();
            }
        }, CFG.authJdbcUrl());
    }

    public List<AuthorityJson> getUserAuthoritiesById(UUID userId) {
        return transaction(connection -> {
            List<AuthAuthorityEntity> authorities =
                    new UserAuthorityDaoJdbc(connection).findAuthoritiesByUserId(userId);

            return authorities.stream()
                    .map(AuthorityJson::fromEntity)
                    .collect(Collectors.toList());
        }, CFG.authJdbcUrl());
    }

    public UserAuthJson updateUser(UserAuthJson user) {
        return transaction(connection -> {
            AuthUserEntity updatedUser = new UserAuthorityDaoJdbc(connection).updateUser(toAuthUserEntityWithId(user));
            return UserAuthJson.fromEntity(updatedUser);
        }, CFG.authJdbcUrl());
    }

    public void deleteUser(String username) {
        transaction(connection -> {
            Optional<AuthUserEntity> user = new UserAuthorityDaoJdbc(connection).findUserByUsername(username);

            if (user.isPresent()) {
                List<AuthAuthorityEntity> authorities =
                        new UserAuthorityDaoJdbc(connection).findAuthoritiesByUserId(user.get().getId());

                for (AuthAuthorityEntity authority : authorities) {
                    new UserAuthorityDaoJdbc(connection).deleteAuthority(authority);
                }

                new UserAuthorityDaoJdbc(connection).deleteUser(user.get());
            }
            return null;
        }, CFG.authJdbcUrl());
    }

    public AuthorityJson createAuthority(String username, String authority) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new UserAuthorityDaoJdbc(connection).findUserByUsername(username);

            if (user.isPresent()) {
                AuthAuthorityEntity authAuthority = new AuthAuthorityEntity();
                authAuthority.setUserId(user.get().getId());
                authAuthority.setAuthority(Authority.valueOf(authority));

                new UserAuthorityDaoJdbc(connection).createAuthority(authAuthority);
                return AuthorityJson.fromEntity(authAuthority);
            } else {
                throw new RuntimeException("User not found: " + username);
            }
        }, CFG.authJdbcUrl());
    }

    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }
}