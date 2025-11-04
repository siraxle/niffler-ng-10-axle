package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.UserAuthorityDaoSpringJdbc;
import guru.qa.niffler.data.dao.impl.*;
import guru.qa.niffler.data.entity.auth.AuthAuthorityEntity;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.UserJson;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.Databases.dataSource;
import static guru.qa.niffler.data.Databases.transaction;
import static guru.qa.niffler.data.entity.user.UserEntity.toUserEntity;

public class UsersDbClient {
    private static final Config CFG = Config.getInstance();

    private static final PasswordEncoder pe = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public UserJson createUserSpringJdbc(UserJson user) {
        AuthUserEntity authUser = new AuthUserEntity();
        authUser.setUsername(user.username());
        authUser.setPassword(pe.encode("12345"));
        authUser.setEnabled(true);
        authUser.setAccountNonExpired(true);
        authUser.setAccountNonLocked(true);
        authUser.setCredentialsNonExpired(true);

        AuthUserEntity createdAuthUser = new AuthUserDaoSpringJdbc(dataSource(CFG.authJdbcUrl()))
                .create(authUser);

        AuthAuthorityEntity[] authorityEntities = Arrays.stream(Authority.values()).map(
                e -> {
                    AuthAuthorityEntity authAuthority = new AuthAuthorityEntity();
                    authAuthority.setUserId(createdAuthUser.getId());
                    authAuthority.setAuthority(e);
                    return authAuthority;
                }
        ).toArray(AuthAuthorityEntity[]::new);

        new AuthAuthorityDaoSpringJdbc(dataSource(CFG.authJdbcUrl()))
                .createAuthority(authorityEntities);
        return UserJson.fromEntity(
                new UdUserDaoSpringJdbc(dataSource(CFG.userdataJdbcUrl()))
                        .create(
                                UserEntity.fromJson(user)
                        )
        );
    }

    public UserJson createUser(UserJson user) {
        return transaction(connection -> {
            UserEntity createdUser = new UserdataUserDaoJdbc(connection).create(toUserEntity(user));
            return UserJson.fromEntity(createdUser);
        }, CFG.userdataJdbcUrl());
    }

    public Optional<UserJson> findUserByUsername(String username) {
        return transaction(connection -> {
            Optional<UserEntity> user = new UserdataUserDaoJdbc(connection).findByUsername(username);
            return user.map(UserJson::fromEntity);
        }, CFG.userdataJdbcUrl());
    }

    public Optional<UserJson> findUserById(UUID id) {
        return transaction(connection -> {
            Optional<UserEntity> user = new UserdataUserDaoJdbc(connection).findById(id);
            return user.map(UserJson::fromEntity);
        }, CFG.userdataJdbcUrl());
    }

    public void deleteUser(String username) {
        transaction(connection -> {
            Optional<UserEntity> user = new UserdataUserDaoJdbc(connection).findByUsername(username);
            user.ifPresent(userEntity -> new UserdataUserDaoJdbc(connection).delete(userEntity));
            return null;
        }, CFG.userdataJdbcUrl());
    }

    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

}