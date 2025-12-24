package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.SearchField;
import io.qameta.allure.Step;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

@ParametersAreNonnullByDefault
public class FriendsPage extends BasePage<FriendsPage> {

    @Getter
    private final SearchField searchField = new SearchField(
            $x("//input[@aria-label='search']"),
            $x("//button[@type='submit']") // или по другому локатору кнопки поиска
    );

    private final SelenideElement friendRequestsTable = $("#requests");
    private final ElementsCollection friendsCollection = $$x("//tbody[@id='requests']/tr");
    private final By unfriendBtn = By.xpath(".//button[text()='Unfriend']");
    private final By acceptBtn = By.xpath(".//button[text()='Accept']");
    private final By declineBtn = By.xpath(".//button[text()='Decline']");
    private final SelenideElement friendsTable = $x("//tbody[@id='friends']");
    private final ElementsCollection requestsRow = friendRequestsTable.$$("tbody tr");
    private final SelenideElement popup = $("div[role='dialog']");
    private final SelenideElement myFriendsTable = $("#friends");

    private final SelenideElement requestsTable = $x("//tbody[@id='requests']");
    private final ElementsCollection requestsCollection = requestsTable.$$("tr");
    private final ElementsCollection myFriendsRows = myFriendsTable.$$("tbody tr");


    @Step("Удалить друга {friendName}")
    @Nonnull
    public FriendsPage removeFriend(String friendName) {
        SelenideElement friendRow = friendsCollection
                .find(text(friendName));
        friendRow.find(unfriendBtn).click();
        return this;
    }

    @Step("Проверить, что друг {friendName} удален")
    @Nonnull
    public FriendsPage verifyFriendRemoved(String friendName) {
        friendsTable.shouldNotHave(text(friendName));
        return this;
    }

    @Step("Проверить существование друга {friendName}")
    public void isFriendNameExist(String friendName) throws InterruptedException {
        Thread.sleep(2000);
        boolean result = friendsCollection
                .findBy(text(friendName))
                .is(visible);
        Assertions.assertTrue(result);
    }

    @Step("Принять запрос в друзья от {requesterName}")
    @Nonnull
    public FriendsPage acceptFriendRequest(String requesterName) {
        SelenideElement requestRow = friendsCollection.findBy(text(requesterName));
        requestRow.find(acceptBtn).click();
        return this;
    }

    @Step("Отклонить запрос в друзья от {requesterName}")
    @Nonnull
    public FriendsPage declineFriendRequest(String requesterName) {
        SelenideElement requestRow = friendsCollection.findBy(text(requesterName));
        requestRow.find(declineBtn).click();
        return this;
    }

    @Step("Проверить существование запроса от {friendName}")
    public void isRequestExist(String friendName) {
        Selenide.sleep(300);
        SelenideElement row = friendsCollection.findBy(text(friendName));
        boolean result = row.find(acceptBtn).is(visible) &&
                row.find(declineBtn).is(visible);
        Assertions.assertTrue(result);
    }

    @Step("Получить количество друзей")
    @Nonnull
    public Integer getFriendsCount() {
        return friendsCollection.size();
    }

    @Step("Проверить количество друзей: ожидается {expectedCount}")
    @Nonnull
    public FriendsPage checkFriendsCount(int expectedCount) {
        friendsCollection.shouldHave(size(expectedCount));
        return this;
    }

    @Step("Поиск друга {friendName}")
    @Nonnull
    public FriendsPage searchFriend(String friendName) {
        searchField.search(friendName);
        return this;
    }

    @Step("Очистить поиск")
    @Nonnull
    public FriendsPage clearSearch() {
        searchField.clearIfNotEmpty();
        return this;
    }

    @Step("Проверить наличие входящего запроса от {username}")
    public void hasIncomeRequest(String username) {
        requestsCollection
                .findBy(text(username))
                .find(acceptBtn)
                .shouldBe(visible);
    }

    @Step("Принять запрос дружбы от пользователя {username}")
    @Nonnull
    public FriendsPage acceptIncomeInvitation(String username) {
        searchField.search(username);
        requestsRow.get(0)
                .find(byText("Accept"))
                .click();
        return this;
    }

    @Step("Отклонить запрос дружбы от пользователя {username}")
    @Nonnull
    public FriendsPage declineIncomeInvitation(String username) {
        searchField.search(username);
        requestsRow.get(0)
                .$$("button[type='button']")
                .get(1)
                .click();
        popup.find(byText("Decline")).click();
        return this;
    }

    @Step("Проверить, что список входящих приглашений пуст")
    @Nonnull
    public FriendsPage checkIncomeInvitationListIsEmpty() {
        requestsRow.first().shouldNotBe(visible);
        return this;
    }

    @Step("Провер|ить, что список друзей не пуст")
    @Nonnull
    public FriendsPage checkFriendsListIsNotEmpty() {
        myFriendsRows.first().shouldBe(visible);
        return this;
    }

}