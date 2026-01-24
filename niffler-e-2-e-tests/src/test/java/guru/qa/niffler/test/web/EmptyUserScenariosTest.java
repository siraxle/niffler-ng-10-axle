package guru.qa.niffler.test.web;

import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ApiLogin;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.*;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Selenide.open;


@ExtendWith({BrowserExtension.class})
public class EmptyUserScenariosTest {

    private static final Config CFG = Config.getInstance();

    @User(username = "empty_user", incomeInvitations = 1)
    @ApiLogin
    @Test
    void shouldHaveFriendRequest(UserJson user) {
        String requesterName = user.testData().incomeInvitations().getFirst().username();

        open(FriendsPage.URL, FriendsPage.class)
                .searchFriend(requesterName)
                .isRequestExist(requesterName);
    }

    @User(username = "empty_user", incomeInvitations = 1)
    @ApiLogin(username = "empty_user")
    @Test
    void shouldFindFriendRequestInTable(UserJson user) throws InterruptedException {
        String requesterName = user.testData().incomeInvitations().getFirst().username();

        open(FriendsPage.URL, FriendsPage.class)
                .searchFriend(requesterName)
                .isFriendNameExist(requesterName);
    }

    @User(username = "empty_user", outcomeInvitations = 1)
    @ApiLogin(username = "empty_user")
    @Test
    void shouldHaveOutcomeRequest(UserJson user) {
        String inviteeUsername = user.testData().outcomeInvitations().getFirst().username();

        open(AllPeoplePage.URL, AllPeoplePage.class)
                .searchPeople(inviteeUsername)
                .hasOutcomeRequest(inviteeUsername);
    }

    @User(username = "empty_user")
    @ApiLogin(username = "empty_user")
    @Test
    void shouldAddNewSpending(UserJson user) {
        String categoryName = "Еда";
        String description = "Обед в ресторане";
        double amount = 1500.00;

        open(UserProfilePage.URL, UserProfilePage.class)
                .addCategory(categoryName)
                .verifyCategoryVisible(categoryName);

        open(EditSpendingPage.URL, EditSpendingPage.class)
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
    @ApiLogin
    public void acceptIncomeInvitation(UserJson user) {
        var incomeInvUsername = user.testData().incomeInvitations().getFirst().username();
        open(FriendsPage.URL, FriendsPage.class)
                .acceptIncomeInvitation(incomeInvUsername)
                .checkFriendsListIsNotEmpty();
    }

    @Test
    @DisplayName("Отклонение заявки в друзья")
    @User(incomeInvitations = 1)
    @ApiLogin
    public void declineIncomeInvitation(UserJson user) {
        var incomeInvUsername = user.testData().incomeInvitations().getFirst().username();
        open(FriendsPage.URL, FriendsPage.class)
                .declineIncomeInvitation(incomeInvUsername)
                .checkIncomeInvitationListIsEmpty();
    }

    @Test
    @DisplayName("Добавление новой траты")
    @User
    @ApiLogin
    public void addNewSpending(UserJson user) {
        var spending = RandomDataUtils.randomSpend(user.username());
        open(EditSpendingPage.URL, EditSpendingPage.class)
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
    @ApiLogin
    public void editProfile(UserJson user) {
        var newUsername = RandomDataUtils.randomUsername();
        var newCategoryName = RandomDataUtils.randomeCategoryName();
        open(UserProfilePage.URL, UserProfilePage.class)
                .checkUsernameDisabled()
                .setName(newUsername)
                .saveChanges()
                .checkName(newUsername)
                .addCategory(newCategoryName)
                .checkCategoryExists(newCategoryName);
    }

}