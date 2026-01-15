package guru.qa.niffler.test.web;

import com.codeborne.selenide.SelenideDriver;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.impl.db.UsersDbClient;
import guru.qa.niffler.utils.SelenideUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BrowserExtension.class, UsersQueueExtension.class})
public class FriendsWebTest {
    private static final Config CFG = Config.getInstance();
    private final SelenideDriver driver = new SelenideDriver(SelenideUtils.chromeConfig);

    @User(
            friends = 1
    )
    @Test
    void friendShouldBePresentInFriendsTable(UserJson user) throws InterruptedException {
        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .isFriendNameExist(user.testData().friends().getFirst().username());
    }

    @User(
            username = "empty_user"
    )
    @Test
    void friendsTableShouldBeEmptyForNewUser(UserJson user) {
        int friendsCount = driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .getFriendsCount();
        Assertions.assertEquals(0, friendsCount);
    }

    @User(
            username = "diana",
            incomeInvitations = 1
    )
    @Test
    void incomeInvitationBePresentInFriendsTable(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .searchFriend(inviterUsername)
                .hasIncomeRequest(inviterUsername);
    }

    @User(
            incomeInvitations = 1
    )
    @Test
    void incomeInvitationBePresentInFriendsTable1(UserJson user) {
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();

        UsersClient usersClient = new UsersDbClient();
        boolean exists = usersClient.findUserByUsername("charlie").isPresent();
        System.out.println("Пользователь " + inviterUsername + " существует в БД: " + exists);
        Assertions.assertTrue(exists);

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .searchFriend(inviterUsername)
                .hasIncomeRequest(inviterUsername);
    }

    @User(
            username = "charlie",
            outcomeInvitations = 1
    )
    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(UserJson user) {
        String outcomeInvitationUsername = user.testData().outcomeInvitations().getFirst().username();
        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader() // Используем Header
                .toAllPeoplesPage()
                .searchPeople(outcomeInvitationUsername)
                .hasOutcomeRequest(outcomeInvitationUsername);
    }

    @User(outcomeInvitations = 1)
    @Test
    void outcomeInvitationBePresentInAllPeopleTable(UserJson user) {
        String inviteeUsername = user.testData().outcomeInvitations().getFirst().username();

        driver.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader() // Используем Header
                .toAllPeoplesPage()
                .searchPeople(inviteeUsername)
                .hasOutcomeRequest(inviteeUsername);
    }
}