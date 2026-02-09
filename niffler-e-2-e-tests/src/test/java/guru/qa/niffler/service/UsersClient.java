package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersClient {

    UserJson createUser(String username, String password);

    Optional<UserJson> findUserByUsername(String username);

    Optional<UserJson> findUserById(UUID id);

    void deleteUser(String username);

    boolean userExists(String username);

    List<UserJson> addIncomeInvitation(UserJson targetUser, int count);

    List<UserJson> addOutcomeInvitation(UserJson targetUser, int count);

    List<UserJson> createFriends(UserJson targetUser, int count);

    List<UserJson> allUsers(String username); // ТОЛЬКО с параметром

    List<UserJson> getFriends(String username);

}