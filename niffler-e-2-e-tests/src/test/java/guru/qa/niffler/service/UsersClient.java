package guru.qa.niffler.service;

import guru.qa.niffler.model.UserJson;

import java.util.Optional;
import java.util.UUID;

public interface UsersClient {

    UserJson createUser(String username, String password);

    Optional<UserJson> findUserByUsername(String username);

    Optional<UserJson> findUserById(UUID id);

    void deleteUser(String username);

    boolean userExists(String username);

    void addIncomeInvitation(UserJson targetUser, int count);

    void addOutcomeInvitation(UserJson targetUser, int count);

    void createFriends(UserJson targetUser, int count);
}