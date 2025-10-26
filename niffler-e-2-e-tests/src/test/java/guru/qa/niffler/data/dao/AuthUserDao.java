package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserDao {
    AuthUserEntity createUser(AuthUserEntity user);

    Optional<AuthUserEntity> findUserById(UUID id);

    Optional<AuthUserEntity> findUserByUsername(String username);

    AuthUserEntity updateUser(AuthUserEntity user);

    void deleteUser(AuthUserEntity user);
}
