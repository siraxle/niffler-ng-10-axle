package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class FriendsPage {
    private final SelenideElement searchInput = $x("//input[@aria-label='search']");
    private final ElementsCollection friendsCollection = $$x("//tbody[@id='friends']/tr");
    private final ElementsCollection allPeopleCollection = $$x("//tbody[@id='requests']/tr");
    private final By unfriendBtn = By.xpath(".//button[text()='Unfriend']");
    private final By acceptBtn = By.xpath(".//button[text()='Accept']");
    private final By declineBtn = By.xpath(".//button[text()='Decline']");
    private final SelenideElement friendsTable = $x("//tbody[@id='friends']");

    public FriendsPage removeFriend(String friendName) {
        SelenideElement friendRow = friendsCollection
                .find(text(friendName));
        friendRow.find(unfriendBtn).click();
        return this;
    }

    public FriendsPage verifyFriendRemoved(String friendName) {
        friendsTable.shouldNotHave(text(friendName));
        return this;
    }

    public void isFriendNameExist(String friendName) throws InterruptedException {
        Thread.sleep(2000);
        boolean result = friendsCollection
                .findBy(text(friendName))
                .is(visible);
        Assertions.assertTrue(result);
    }

    public FriendsPage acceptFriendRequest(String requesterName) {
        SelenideElement requestRow = friendsCollection.findBy(text(requesterName));
        requestRow.find(acceptBtn).click();
        return this;
    }

    public FriendsPage declineFriendRequest(String requesterName) {
        SelenideElement requestRow = friendsCollection.findBy(text(requesterName));
        requestRow.find(declineBtn).click();
        return this;
    }

    public void isRequestExist(String friendName) {
        SelenideElement row = friendsCollection.findBy(text(friendName));
        boolean result = row.find(acceptBtn).is(visible) &&
                row.find(declineBtn).is(visible);
        Assertions.assertTrue(result);
    }

    public Integer getFriendsCount() {
        return friendsCollection.size();
    }

    public FriendsPage checkFriendsCount(int expectedCount) {
        friendsCollection.shouldHave(size(expectedCount));
        return this;
    }

    public FriendsPage searchFriend(String friendName) {
        searchInput.setValue(friendName);
        return this;
    }

    public void hasIncomeRequest(String incomeFriendName) throws InterruptedException {
        Thread.sleep(2000);
        boolean result = allPeopleCollection
                .findBy(text(incomeFriendName))
                .find(acceptBtn)
                .is(visible);
        Assertions.assertTrue(result);
    }
}
