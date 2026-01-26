package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.AllPeoplePage;
import guru.qa.niffler.page.FriendsPage;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.impl.db.UsersDbClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BrowserExtension.class, UsersQueueExtension.class})
public class FriendsWebTest {
    private static final Config CFG = Config.getInstance();

    @User(friends = 1)
    @ApiLogin
    @Test
    void friendShouldBePresentInFriendsTable(UserJson user) throws InterruptedException {
        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .isFriendNameExist(user.testData().friends().getFirst().username());
    }

    @User(username = "empty_user")
    @ApiLogin(username = "empty_user")
    @Test
    void friendsTableShouldBeEmptyForNewUser(UserJson user) {
        int friendsCount = Selenide.open(FriendsPage.URL, FriendsPage.class)
                .getFriendsCount();
        Assertions.assertEquals(0, friendsCount);
    }

    @User(username = "diana", incomeInvitations = 1)
    @ApiLogin
    @Test
    void incomeInvitationBePresentInFriendsTable(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();

        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .searchFriend(inviterUsername)
                .hasIncomeRequest(inviterUsername);
    }

    @User(incomeInvitations = 1)
    @ApiLogin
    @Test
    void incomeInvitationBePresentInFriendsTable1(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();

        UsersClient usersClient = new UsersDbClient();
        boolean exists = usersClient.findUserByUsername("charlie").isPresent();
        System.out.println("Пользователь " + inviterUsername + " существует в БД: " + exists);
        Assertions.assertTrue(exists);

        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .searchFriend(inviterUsername)
                .hasIncomeRequest(inviterUsername);
    }

    @User(outcomeInvitations = 1)
    @ApiLogin(username = "charlie")
    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(UserJson user) {
        String outcomeInvitationUsername = user.testData().outcomeInvitations().getFirst().username();
        Selenide.open(AllPeoplePage.URL, AllPeoplePage.class)
                .searchPeople(outcomeInvitationUsername)
                .hasOutcomeRequest(outcomeInvitationUsername);
    }

    @User(outcomeInvitations = 1)
    @ApiLogin
    @Test
    void outcomeInvitationBePresentInAllPeopleTable(UserJson user) {
        String inviteeUsername = user.testData().outcomeInvitations().getFirst().username();

        Selenide.open(AllPeoplePage.URL, AllPeoplePage.class)
                .searchPeople(inviteeUsername)
                .hasOutcomeRequest(inviteeUsername);
    }
}