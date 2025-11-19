package guru.qa.niffler.data.dao;

import guru.qa.niffler.data.entity.auth.AuthUserEntity;
import guru.qa.niffler.data.entity.auth.AuthorityEntity;
import guru.qa.niffler.data.entity.user.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAuthorityDao {
    void createAuthority(AuthorityEntity... authority);
    List<AuthorityEntity> findAuthoritiesByUserId(UUID userId);
    void deleteAuthority(AuthorityEntity authority);

    UserEntity createUser(UserEntity user);
    Optional<UserEntity> findById(UUID id);
    Optional<UserEntity> findByUsername(String username);
    void delete(UserEntity user);

    AuthUserEntity createUser(AuthUserEntity user);
    Optional<AuthUserEntity> findUserById(UUID id);
    Optional<AuthUserEntity> findUserByUsername(String username);
    AuthUserEntity updateUser(AuthUserEntity user);
    void deleteUser(AuthUserEntity user);

}
