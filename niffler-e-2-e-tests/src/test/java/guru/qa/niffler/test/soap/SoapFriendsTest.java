package guru.qa.niffler.test.soap;

import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.annotation.meta.SoapTest;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.service.impl.api.UsersApiClient;
import guru.qa.niffler.service.impl.api.UsersSoapClient;
import guru.qa.jaxb.userdata.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static guru.qa.niffler.utils.RandomDataUtils.randomUsername;

@SoapTest
public class SoapFriendsTest {

    private final UsersSoapClient usersSoapClient = new UsersSoapClient();
    private final UsersApiClient usersApiClient = new UsersApiClient();

    @Test
    @DisplayName("Список друзей получен в виде Page при передаче параметров page, size")
    @User(friends = 3)
    void friendsListShouldBeReturnedWithPagination(UserJson user) throws IOException {
        FriendsPageRequest request = new FriendsPageRequest();
        request.setUsername(user.username());

        PageInfo pageInfo = new PageInfo();
        pageInfo.setPage(0);
        pageInfo.setSize(2);
        request.setPageInfo(pageInfo);

        UsersResponse response = usersSoapClient.friendsPage(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(2, response.getUser().size(), "Должно быть 2 друга на странице");
        Assertions.assertEquals(3, response.getTotalElements(), "Всего должно быть 3 друга");
        Assertions.assertEquals(2, response.getSize(), "Page size должен быть 2");
        Assertions.assertEquals(0, response.getNumber(), "Page number должен быть 0");
        Assertions.assertTrue(response.getTotalPages() >= 2, "Должна быть минимум 2 страницы");
    }

    @Test
    @DisplayName("Для пользователя должен возвращаться список друзей с фильтраций по username, если передан searchQuery")
    @User(friends = 3)
    void friendsListShouldBeFilteredByUsername(UserJson user) throws IOException {
        String friendUsername = user.testData().friends().get(0).username();
        String searchQuery = friendUsername.substring(1, friendUsername.length() - 1);

        FriendsRequest request = new FriendsRequest();
        request.setUsername(user.username());
        request.setSearchQuery(searchQuery);

        UsersResponse response = usersSoapClient.friends(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.getUser().size(), "Должен найтись только один друг");
        Assertions.assertEquals(friendUsername, response.getUser().get(0).getUsername());
    }

    @Test
    @DisplayName("Дружба должна удаляться")
    @User(friends = 1)
    void friendshipShouldBeRemoved(UserJson user) throws IOException {
        UserJson friend = user.testData().friends().get(0);

        RemoveFriendRequest removeRequest = new RemoveFriendRequest();
        removeRequest.setUsername(user.username());
        removeRequest.setFriendToBeRemoved(friend.username());

        usersSoapClient.removeFriend(removeRequest);

        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setUsername(user.username());
        UsersResponse friendsAfter = usersSoapClient.friends(friendsRequest);

        Assertions.assertTrue(
                friendsAfter.getUser().stream()
                        .noneMatch(u -> u.getUsername().equals(friend.username())),
                "Друг должен быть удален"
        );
    }

    @Test
    @DisplayName("Прием заявки в друзья")
    @User(incomeInvitations = 1)
    void shouldAcceptFriendshipInvitation(UserJson user) throws IOException {
        UserJson requester = user.testData().incomeInvitations().get(0);

        AcceptInvitationRequest acceptRequest = new AcceptInvitationRequest();
        acceptRequest.setUsername(user.username());
        acceptRequest.setFriendToBeAdded(requester.username());

        UserResponse acceptResponse = usersSoapClient.acceptInvitation(acceptRequest);

        Assertions.assertNotNull(acceptResponse);
        Assertions.assertNotNull(acceptResponse.getUser());
        Assertions.assertEquals(requester.username(), acceptResponse.getUser().getUsername());

        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setUsername(user.username());
        UsersResponse friendsAfter = usersSoapClient.friends(friendsRequest);

        Assertions.assertTrue(
                friendsAfter.getUser().stream()
                        .anyMatch(u -> u.getUsername().equals(requester.username())),
                "Отправитель приглашения должен стать другом"
        );
    }

    @Test
    @DisplayName("Отклонение заявки в друзья")
    @User(incomeInvitations = 1)
    void shouldDeclineFriendshipInvitation(UserJson user) throws IOException {
        UserJson requester = user.testData().incomeInvitations().get(0);

        DeclineInvitationRequest declineRequest = new DeclineInvitationRequest();
        declineRequest.setUsername(user.username());
        declineRequest.setInvitationToBeDeclined(requester.username());

        UserResponse declineResponse = usersSoapClient.declineInvitation(declineRequest);

        Assertions.assertNotNull(declineResponse.getUser());
        Assertions.assertEquals(requester.username(), declineResponse.getUser().getUsername());

        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setUsername(user.username());
        UsersResponse friendsAfter = usersSoapClient.friends(friendsRequest);

        Assertions.assertTrue(
                friendsAfter.getUser().stream()
                        .noneMatch(u -> u.getUsername().equals(requester.username())),
                "Отклонивший пользователь не должен быть в друзьях"
        );
    }

    @Test
    @DisplayName("Отправка приглашения дружить")
    @User
    void shouldSendFriendshipInvitation(UserJson user) throws IOException {
        String targetUsername = randomUsername();
        usersApiClient.createUser(targetUsername, "123456");

        SendInvitationRequest sendRequest = new SendInvitationRequest();
        sendRequest.setUsername(user.username());
        sendRequest.setFriendToBeRequested(targetUsername);

        UserResponse sendResponse = usersSoapClient.sendInvitation(sendRequest);

        Assertions.assertNotNull(sendResponse.getUser());
        Assertions.assertEquals(targetUsername, sendResponse.getUser().getUsername());
    }
}