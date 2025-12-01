package guru.qa.niffler.data.repository;

import guru.qa.niffler.data.entity.user.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDataUserRepository {
    UserEntity create(UserEntity user);

    Optional<UserEntity> findById(UUID id);

    Optional<UserEntity> findByUsername(String username);

    UserEntity update(UserEntity user);

    void remove(UserEntity user);

    List<UserEntity> findAll();

    // методы для работы с друзьями
    void addIncomeInvitation(UserEntity requester, UserEntity addressee);

    void addFriend(UserEntity requester, UserEntity addressee);

    void addOutcomeInvitation(UserEntity requester, UserEntity addressee);

    void removeFriend(UserEntity user, UserEntity friend);

    List<UserEntity> findFriends(UserEntity user);

    List<UserEntity> findPendingInvitations(UserEntity user);

    void acceptFriend(UserEntity user, UserEntity friend);

}