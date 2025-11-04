package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.AuthAuthorityDaoJdbc;
import guru.qa.niffler.data.dao.impl.AuthUserDaoJdbc;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.model.Authority;
import guru.qa.niffler.model.AuthorityJson;
import guru.qa.niffler.model.UserAuthJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static guru.qa.niffler.data.Databases.transaction;
import static guru.qa.niffler.data.entity.auth.AuthUserEntity.toAuthUserEntity;

public class AuthDbClient {
    private static final Config CFG = Config.getInstance();

    public UserAuthJson createUser(UserAuthJson user) {
        return transaction(connection -> {
            AuthUserEntity createdUser = new AuthUserDaoJdbc(connection).create(toAuthUserEntity(user));

            AuthorityEntity readAuthority = new AuthorityEntity();
            readAuthority.setUserId(createdUser.getId());
            readAuthority.setAuthority(Authority.READ);

            AuthorityEntity writeAuthority = new AuthorityEntity();
            writeAuthority.setUserId(createdUser.getId());
            writeAuthority.setAuthority(Authority.WRITE);

            new AuthAuthorityDaoJdbc(connection).create(readAuthority, writeAuthority);

            return UserAuthJson.fromEntity(createdUser);
        }, CFG.authJdbcUrl());
    }

    public Optional<UserAuthJson> findUserByUsername(String username) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new AuthUserDaoJdbc(connection).findByUsername(username);
            return user.map(UserAuthJson::fromEntity);
        }, CFG.authJdbcUrl());
    }

    // Поиск пользователя по ID
    public Optional<UserAuthJson> findUserById(UUID id) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new AuthUserDaoJdbc(connection).findById(id);
            return user.map(UserAuthJson::fromEntity);
        }, CFG.authJdbcUrl());
    }

    // Получение authorities пользователя
    public List<AuthorityJson> getUserAuthorities(String username) {
        return transaction(connection -> {
            Optional<AuthUserEntity> user = new AuthUserDaoJdbc(connection).findByUsername(username);

            if (user.isPresent()) {
                List<AuthorityEntity> authorities =
                        new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(user.get().getId());

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
            List<AuthorityEntity> authorities =
                    new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(userId);

            return authorities.stream()
                    .map(AuthorityJson::fromEntity)
                    .collect(Collectors.toList());
        }, CFG.authJdbcUrl());
    }

    public UserAuthJson updateUser(UserAuthJson user) {
        return transaction(connection -> {
            AuthUserEntity userEntity = new AuthUserEntity();
            userEntity.setId(user.id());
            userEntity.setUsername(user.username());
            userEntity.setPassword(user.password());
            userEntity.setEnabled(user.enabled());
            userEntity.setAccountNonExpired(user.accountNonExpired());
            userEntity.setAccountNonLocked(user.accountNonLocked());
            userEntity.setCredentialsNonExpired(user.credentialsNonExpired());

            AuthUserEntity updatedUser = new AuthUserDaoJdbc(connection).update(userEntity);
            return UserAuthJson.fromEntity(updatedUser);
        }, CFG.authJdbcUrl());
    }

    public void deleteUser(String username) {
        transaction(connection -> {
            Optional<AuthUserEntity> user = new AuthUserDaoJdbc(connection).findByUsername(username);

            if (user.isPresent()) {
                // Сначала удаляем authorities
                List<AuthorityEntity> authorities =
                        new AuthAuthorityDaoJdbc(connection).findAuthoritiesByUserId(user.get().getId());

                for (AuthorityEntity authority : authorities) {
                    new AuthAuthorityDaoJdbc(connection).deleteAuthority(authority);
                }

                // Затем удаляем пользователя
                new AuthUserDaoJdbc(connection).delete(user.get());
            }
            return null; // Consumer требует возвращаемое значение
        }, CFG.authJdbcUrl());

        // Удаляем из userdata БД
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