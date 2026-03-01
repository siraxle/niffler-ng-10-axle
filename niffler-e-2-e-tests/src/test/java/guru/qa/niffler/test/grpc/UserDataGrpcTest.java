package guru.qa.niffler.test.grpc;

import guru.qa.niffler.grpc.*;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.GrpcTest;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
public class UserDataGrpcTest extends BaseGgrpcTest {

    @User(friends = 5)
    @Test
    @DisplayName("Список друзей получен в виде Page при передаче параметров page, size")
    void shouldReturnFriendsAsPageWithPagination(UserJson user) {
        String username = user.username();

        GetFriendsRequest request = GetFriendsRequest.newBuilder()
                .setUsername(username)
                .setPage(0)
                .setSize(2)
                .addSort("username,asc")
                .build();

        UserPageResponse response = userdataStub.getFriends(request);

        assertNotNull(response);
        assertTrue(response.getTotalElements() >= 5);
        assertTrue(response.getTotalPages() >= 3);
        assertTrue(response.getFirst());
        assertEquals(2, response.getEdgesList().size());

        GetFriendsRequest secondPageRequest = GetFriendsRequest.newBuilder()
                .setUsername(username)
                .setPage(1)
                .setSize(2)
                .addSort("username,asc")
                .build();

        UserPageResponse secondPageResponse = userdataStub.getFriends(secondPageRequest);

        assertNotNull(secondPageResponse);
        assertEquals(2, secondPageResponse.getEdgesList().size());
        assertNotEquals(
                response.getEdgesList().get(0).getUsername(),
                secondPageResponse.getEdgesList().get(0).getUsername()
        );
    }

    @User(friends = 3)
    @Test
    @DisplayName("Для пользователя должен возвращаться список друзей с фильтрацией по username, если передан searchQuery")
    void shouldFilterFriendsByUsernameSearchQuery(UserJson user) {
        String username = user.username();
        String friendUsername = user.testData().friends().get(0).username();
        String searchQuery = friendUsername.substring(0, 3);

        GetFriendsListRequest request = GetFriendsListRequest.newBuilder()
                .setUsername(username)
                .setSearchQuery(searchQuery)
                .build();

        UserListResponse response = userdataStub.getFriendsList(request);

        assertNotNull(response);
        assertTrue(response.getUsersList().size() > 0);
        assertTrue(response.getUsersList().stream()
                .allMatch(u -> u.getUsername().contains(searchQuery)));
    }

    @User(friends = 1)
    @Test
    @DisplayName("Дружба должна удаляться")
    void shouldRemoveFriend(UserJson user) {
        String username = user.username();
        String friendUsername = user.testData().friends().get(0).username();

        GetFriendsListRequest checkBeforeRequest = GetFriendsListRequest.newBuilder()
                .setUsername(username)
                .build();

        UserListResponse beforeResponse = userdataStub.getFriendsList(checkBeforeRequest);
        assertTrue(beforeResponse.getUsersList().stream()
                .anyMatch(u -> u.getUsername().equals(friendUsername)));

        RemoveFriendRequest removeRequest = RemoveFriendRequest.newBuilder()
                .setUsername(username)
                .setTargetUsername(friendUsername)
                .build();

        userdataStub.removeFriend(removeRequest);

        GetFriendsListRequest checkAfterRequest = GetFriendsListRequest.newBuilder()
                .setUsername(username)
                .build();

        UserListResponse afterResponse = userdataStub.getFriendsList(checkAfterRequest);
        assertTrue(afterResponse.getUsersList().stream()
                .noneMatch(u -> u.getUsername().equals(friendUsername)));
    }

    @User(incomeInvitations = 2)
    @Test
    @DisplayName("Прием заявки в друзья")
    void shouldAcceptFriendshipRequest(UserJson user) {
        String username = user.username();
        String senderUsername = user.testData().incomeInvitations().get(0).username();

        AcceptFriendshipRequestRequest acceptRequest = AcceptFriendshipRequestRequest.newBuilder()
                .setUsername(username)
                .setTargetUsername(senderUsername)
                .build();

        UserResponse response = userdataStub.acceptFriendshipRequest(acceptRequest);

        assertNotNull(response);
        assertEquals(senderUsername, response.getUsername());
        assertEquals(FriendshipStatus.FRIEND, response.getFriendshipStatus());

        GetFriendsListRequest checkRequest = GetFriendsListRequest.newBuilder()
                .setUsername(username)
                .build();

        UserListResponse friendsResponse = userdataStub.getFriendsList(checkRequest);
        assertTrue(friendsResponse.getUsersList().stream()
                .anyMatch(u -> u.getUsername().equals(senderUsername)));
    }

    @User(incomeInvitations = 2)
    @Test
    @DisplayName("Отклонение заявки в друзья")
    void shouldDeclineFriendshipRequest(UserJson user) {
        String username = user.username();
        String senderUsername = user.testData().incomeInvitations().get(0).username();

        GetFriendsListRequest checkBeforeRequest = GetFriendsListRequest.newBuilder()
                .setUsername(username)
                .build();

        UserListResponse beforeResponse = userdataStub.getFriendsList(checkBeforeRequest);
        assertTrue(beforeResponse.getUsersList().stream()
                .anyMatch(u -> u.getUsername().equals(senderUsername) &&
                        u.getFriendshipStatus() == FriendshipStatus.INVITE_RECEIVED));

        DeclineFriendshipRequestRequest declineRequest = DeclineFriendshipRequestRequest.newBuilder()
                .setUsername(username)
                .setTargetUsername(senderUsername)
                .build();

        UserResponse response = userdataStub.declineFriendshipRequest(declineRequest);

        assertNotNull(response);
        assertEquals(senderUsername, response.getUsername());

        GetFriendsListRequest checkAfterRequest = GetFriendsListRequest.newBuilder()
                .setUsername(username)
                .build();

        UserListResponse afterResponse = userdataStub.getFriendsList(checkAfterRequest);
        assertTrue(afterResponse.getUsersList().stream()
                .noneMatch(u -> u.getUsername().equals(senderUsername)));
    }

    @User
    @Test
    @DisplayName("Отправка приглашения дружить через gRPC")
    void shouldSendFriendshipRequestViaGrpc(UserJson user) {
        String username = user.username();
        String targetUsername = RandomDataUtils.randomUsername();

        userdataStub.updateUser(UpdateUserRequest.newBuilder()
                .setUsername(targetUsername)
                .setCurrency(CurrencyValues.RUB)
                .build());

        SendFriendshipRequestRequest sendRequest = SendFriendshipRequestRequest.newBuilder()
                .setUsername(username)
                .setTargetUsername(targetUsername)
                .build();

        UserResponse sendResponse = userdataStub.sendFriendshipRequest(sendRequest);

        assertNotNull(sendResponse);
        assertEquals(targetUsername, sendResponse.getUsername());
        assertEquals(FriendshipStatus.INVITE_SENT, sendResponse.getFriendshipStatus());

        GetFriendsListRequest checkTargetRequest = GetFriendsListRequest.newBuilder()
                .setUsername(targetUsername)
                .build();
        UserListResponse targetResponse = userdataStub.getFriendsList(checkTargetRequest);

        boolean incomeInvitationFound = targetResponse.getUsersList().stream()
                .anyMatch(u -> u.getUsername().equals(username) &&
                        u.getFriendshipStatus() == FriendshipStatus.INVITE_RECEIVED);

        assertTrue(incomeInvitationFound,
                "У целевого пользователя должно появиться входящее приглашение");
    }
}