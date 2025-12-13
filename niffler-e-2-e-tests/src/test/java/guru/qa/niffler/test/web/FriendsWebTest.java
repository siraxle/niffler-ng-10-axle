package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.service.UsersClient;
import guru.qa.niffler.service.impl.db.UsersDbClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BrowserExtension.class, UsersQueueExtension.class})
public class FriendsWebTest {
    private static final Config CFG = Config.getInstance();

    @User(
            friends = 1
    )
    @Test
    void friendShouldBePresentInFriendsTable(UserJson user) throws InterruptedException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToFriendsPage()
                .isFriendNameExist(user.testData().friends().getFirst().username());
    }

    @User(
            username = "empty_user"
    )
    @Test
    void friendsTableShouldBeEmptyForNewUser(UserJson user) {
        int friendsCount = Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToFriendsPage()
                .getFriendsCount();
        Assertions.assertEquals(0, friendsCount);
    }

    //TODO Тест падает, хотя в БД incomeInvitations есть, на фронт не подтягивается incomeInvitations
    @User(
            username = "diana",
            incomeInvitations = 1 // Создаем входящее приглашение
    )
    @Test
    void incomeInvitationBePresentInFriendsTable(UserJson user) throws InterruptedException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToFriendsPage()
                .hasIncomeRequest(user.testData().incomeInvitations().getFirst().username());
    }


    //TODO Тест падает, хотя в БД incomeInvitations есть, на фронт не подтягивается incomeInvitations
    @User(
            incomeInvitations = 1
    )
    @Test
    void incomeInvitationBePresentInFriendsTable1(UserJson user) throws InterruptedException {
        // новый пользователь + 1 приглашение
        String inviterUsername = user.testData().incomeInvitations().getFirst().username();

        UsersClient usersClient = new UsersDbClient();
        boolean exists = usersClient.findUserByUsername("charlie").isPresent();
        System.out.println("Пользователь " + inviterUsername + " существует в БД: " + exists);
        Assertions.assertTrue(exists);

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToFriendsPage()
                .hasIncomeRequest(inviterUsername);
    }

    @User(
            username = "charlie",
            outcomeInvitations = 1
    )
    @Test
    void outcomeInvitationBePresentInAllPeoplesTable(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToAllPeoplePage()
                .hasOutcomeRequest(user.testData().outcomeInvitations().getFirst().username());
    }

    @User(outcomeInvitations = 1)
    @Test
    void outcomeInvitationBePresentInAllPeopleTable(UserJson user) {
        String inviteeUsername = user.testData().outcomeInvitations().getFirst().username();

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .goToAllPeoplePage()
                .hasOutcomeRequest(inviteeUsername);
    }

}