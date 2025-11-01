package guru.qa.niffler.service;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.data.dao.impl.UserdataUserDaoJdbc;
import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.model.UserJson;

import java.util.Optional;
import java.util.UUID;

import static guru.qa.niffler.data.Databases.transaction;

public class UsersDbClient {
    private static final Config CFG = Config.getInstance();

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

    private UserEntity toUserEntity(UserJson user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.id());
        entity.setUsername(user.username());
        entity.setCurrency(user.currency());
        entity.setFirstname(user.firstname());
        entity.setSurname(user.surname());
        entity.setFullname(user.fullname());
        entity.setPhoto(user.photo());
        entity.setPhotoSmall(user.photoSmall());
        return entity;
    }
}