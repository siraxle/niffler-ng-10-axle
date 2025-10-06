package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static guru.qa.niffler.jupiter.extension.UsersQueueExtension.UserType.Type.*;

@ExtendWith({BrowserExtension.class, UsersQueueExtension.class})
public class FriendsWebTest {
    private static final Config CFG = Config.getInstance();

    @Test
    void friendShouldBePresentInFriendsTable(@UsersQueueExtension.UserType(WITH_FRIEND) UsersQueueExtension.StaticUser user) throws InterruptedException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.password())
                .goToFriendsPage()
                .isFriendNameExist(user.friend());// проверяем что bob есть в друзьях
    }

    @Test
    void friendsTableShouldBeEmptyForNewUser(@UsersQueueExtension.UserType(EMPTY) UsersQueueExtension.StaticUser user) {
        int friendsCount = Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.password())
                .goToFriendsPage()
                .getFriendsCount();
        Assertions.assertEquals(0, friendsCount);
    }

    @Test
    void incomeInvitationBePresentInFriendsTable(@UsersQueueExtension.UserType(WITH_INCOME_REQUEST) UsersQueueExtension.StaticUser user) throws InterruptedException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.password())
                .goToFriendsPage()
                .hasIncomeRequest(user.income()); // проверяем что есть запрос от charlie
    }

    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(@UsersQueueExtension.UserType(WITH_OUTCOME_REQUEST) UsersQueueExtension.StaticUser user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.password())
                .goToAllPeoplePage()
                .hasOutcomeRequest(user.outcome()); // проверяем что есть исходящий запрос к diana
    }
}