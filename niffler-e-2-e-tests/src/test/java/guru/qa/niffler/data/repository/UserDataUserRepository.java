package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.user.UserEntity;
import guru.qa.niffler.data.repository.impl.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDataUserRepository {

    static UserDataUserRepository getInstance() {
        return switch (System.getProperty("repository", "jpa")) {
            case "jpa" -> new UserdataUserRepositoryHibernate();
            case "jdbc" -> new UserdataRepositoryJdbc();
            case "sjdbc" -> new UserdataRepositorySpringJdbc();
            default -> throw  new IllegalStateException("Unrecognized repository: " + System.getProperty("repository"));
        };
    }

    UserEntity create(UserEntity user);

    Optional<UserEntity> findById(UUID id);

    Optional<UserEntity> findByUsername(String username);

    UserEntity update(UserEntity user);

    void remove(UserEntity user);

    List<UserEntity> findAll();

    // методы для работы с друзьями
    void addInvitation(UserEntity user1, UserEntity user2);

    void addFriend(UserEntity requester, UserEntity addressee);

    void removeFriend(UserEntity user, UserEntity friend);

    List<UserEntity> findFriends(UserEntity user);

    List<UserEntity> findPendingInvitations(UserEntity user);

    void acceptFriend(UserEntity user, UserEntity friend);

}