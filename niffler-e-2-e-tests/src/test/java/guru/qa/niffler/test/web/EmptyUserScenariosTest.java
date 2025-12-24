package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.UsersQueueExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.open;


@ExtendWith({BrowserExtension.class, UsersQueueExtension.class})
public class EmptyUserScenariosTest {

    private static final Config CFG = Config.getInstance();

    @User(
            username = "empty_user",
            incomeInvitations = 1
    )
    @Test
    void shouldHaveFriendRequest(UserJson user) {
        String requesterName = user.testData().incomeInvitations().getFirst().username();

        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .searchFriend(requesterName)
                .isRequestExist(requesterName);
    }

    @User(
            username = "empty_user",
            incomeInvitations = 1
    )
    @Test
    void shouldFindFriendRequestInTable(UserJson user) throws InterruptedException {
        String requesterName = user.testData().incomeInvitations().getFirst().username();

        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .searchFriend(requesterName)
                .isFriendNameExist(requesterName); // Проверяем наличие в таблице
    }

    @User(
            username = "empty_user",
            outcomeInvitations = 1
    )
    @Test
    void shouldHaveOutcomeRequest(UserJson user) {
        String inviteeUsername = user.testData().outcomeInvitations().getFirst().username();

        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toAllPeoplesPage()
                .searchPeople(inviteeUsername)
                .hasOutcomeRequest(inviteeUsername);
    }

    @User(
            username = "empty_user"
    )
    @Test
    void shouldAddNewSpending(UserJson user) {
        String categoryName = "Еда";
        String description = "Обед в ресторане";
        double amount = 1500.00;

        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .addCategory(categoryName)
                .verifyCategoryVisible(categoryName);

        open(CFG.frontUrl(), MainPage.class)
                .getHeader()
                .toAddSpendingPage()
                .setCategory(categoryName)
                .setAmount(amount)
                .setDescription(description)
                .save()
                .getSpendingTable()
                .checkThatTableContains(description)
                .checkTableSize(1);
    }

    @Test
    @DisplayName("Принятие заявки в друзья")
    @User(incomeInvitations = 1)
    public void acceptIncomeInvitation(UserJson user) {
        var incomeInvUsername = user.testData().incomeInvitations().getFirst().username();
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .acceptIncomeInvitation(incomeInvUsername)
                .checkFriendsListIsNotEmpty();
    }

    @Test
    @DisplayName("Отклонение заявки в друзья")
    @User(incomeInvitations = 1)
    public void declineIncomeInvitation(UserJson user) {
        var incomeInvUsername = user.testData().incomeInvitations().getFirst().username();
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toFriendsPage()
                .declineIncomeInvitation(incomeInvUsername)
                .checkIncomeInvitationListIsEmpty();
    }

    @Test
    @DisplayName("Добавление новой траты")
    @User
    public void addNewSpending(UserJson user) {
        var spending = RandomDataUtils.randomSpend(user.username());
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toAddSpendingPage()
                .setAmount(spending.amount())
                .setCategory(spending.category().name())
                .setNewSpendingDescription(spending.description())
                .save()
                .getSpendingTable()
                .checkThatTableContains(spending.description());
    }

    @Test
    @DisplayName("Редактирование профиля")
    @User
    public void editProfile(UserJson user) {
        var newUsername = RandomDataUtils.randomUsername();
        var newCategoryName = RandomDataUtils.randomeCategoryName();
        open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getHeader()
                .toProfilePage()
                .checkUsernameDisabled()
                .setName(newUsername)
                .saveChanges()
                .checkName(newUsername)
                .addCategory(newCategoryName)
                .checkCategoryExists(newCategoryName);
    }

}