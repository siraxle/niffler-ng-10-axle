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

    @ApiLogin(username = "diana", password = "123456")
    @Test
    void friendsTableShouldBeEmptyForNewUser() {
        int friendsCount = Selenide.open(FriendsPage.URL, FriendsPage.class)
                .getFriendsCount();
        Assertions.assertEquals(0, friendsCount);
    }

    @User(incomeInvitations = 1)
    @ApiLogin
    @Test
    void incomeInvitationBePresentInFriendsTable1(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();

        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .searchFriend(inviterUsername)
                .hasIncomeRequest(inviterUsername);
    }

    @User(outcomeInvitations = 1)
    @ApiLogin
    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(UserJson user) {
        String outcomeInvitationUsername = user.testData().outcomeInvitations().getFirst().username();
        Selenide.open(AllPeoplePage.URL, AllPeoplePage.class)
                .searchPeople(outcomeInvitationUsername)
                .hasOutcomeRequest(outcomeInvitationUsername);
    }

    @User(incomeInvitations = 1)
    @ApiLogin
    @Test
    void acceptIncomeInvitation(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();
        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .acceptIncomeInvitation(inviterUsername)
                .isFriendNameExist(inviterUsername);
    }

    @User(incomeInvitations = 1)
    @ApiLogin
    @Test
    void declineIncomeInvitation(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();
        Selenide.open(FriendsPage.URL, FriendsPage.class)
                .declineIncomeInvitation(inviterUsername)
                .checkIncomeInvitationListIsEmpty();
    }


}