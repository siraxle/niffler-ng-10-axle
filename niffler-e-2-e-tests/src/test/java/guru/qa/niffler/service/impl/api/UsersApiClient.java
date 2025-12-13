package guru.qa.niffler.service.impl.api;

import guru.qa.niffler.api.UserApi;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.UsersClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsersApiClient implements UsersClient {

    private static final Config CFG = Config.getInstance();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(CFG.userdataUrl())
            .addConverterFactory(JacksonConverterFactory.create())
            .build();

    private final UserApi userApi = retrofit.create(UserApi.class);

    @Override
    public Optional<UserJson> findUserByUsername(String username) {
        try {
            Response<UserJson> response = userApi.currentUser(username).execute();
            if (response.isSuccessful()) {
                return Optional.ofNullable(response.body());
            } else if (response.code() == 404) {
                return Optional.empty();
            } else {
                throw new RuntimeException("Failed to find user. Code: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to find user", e);
        }
    }

    @Override
    public UserJson createUser(String username, String password) {
        throw new UnsupportedOperationException("Create user via REST API not implemented");
    }

    @Override
    public List<UserJson> createFriends(UserJson targetUser, int count) {
        throw new UnsupportedOperationException("Create friends via REST API not implemented");
    }

    @Override
    public Optional<UserJson> findUserById(UUID id) {
        throw new UnsupportedOperationException("Find user by ID via REST API not implemented");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Delete user via REST API not implemented");
    }

    @Override
    public boolean userExists(String username) {
        return findUserByUsername(username).isPresent();
    }

    @Override
    public List<UserJson> addIncomeInvitation(UserJson targetUser, int count) {
        // Нет endpoint'а для массового создания входящих приглашений
        // Можно эмулировать через несколько вызовов sendInvitation от разных пользователей
        throw new UnsupportedOperationException("Add income invitations via REST API not implemented");
    }

    @Override
    public List<UserJson> addOutcomeInvitation(UserJson targetUser, int count) {
        // Нет endpoint'а для массового создания исходящих приглашений
        // Можно эмулировать через несколько вызовов sendInvitation от targetUser
        throw new UnsupportedOperationException("Add outcome invitations via REST API not implemented");
    }

    public UserJson updateUserInfo(UserJson user) {
        try {
            Response<UserJson> response = userApi.updateUserInfo(user).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to update user. Code: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public List<UserJson> allUsers(String username, String searchQuery) {
        try {
            Response<List<UserJson>> response = userApi.allUsers(username, searchQuery).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get all users. Code: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get all users", e);
        }
    }

    public UserJson sendInvitation(String username, String targetUsername) {
        try {
            Response<UserJson> response = userApi.sendInvitation(username, targetUsername).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to send invitation. Code: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("Failed to send invitation", e);
        }
    }

    public UserJson acceptInvitation(String username, String targetUsername) {
        try {
            Response<UserJson> response = userApi.acceptInvitation(username, targetUsername).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to accept invitation. Code: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("Failed to accept invitation", e);
        }
    }

    public UserJson declineInvitation(String username, String targetUsername) {
        try {
            Response<UserJson> response = userApi.declineInvitation(username, targetUsername).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to decline invitation. Code: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("Failed to decline invitation", e);
        }
    }

    public void removeFriend(String username, String targetUsername) {
        try {
            Response<Void> response = userApi.removeFriend(username, targetUsername).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to remove friend. Code: " + response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to remove friend", e);
        }
    }

    public List<UserJson> friends(String username, String searchQuery) {
        try {
            Response<List<UserJson>> response = userApi.friends(username, searchQuery).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get friends. Code: " + response.code());
            }
            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get friends", e);
        }
    }
}