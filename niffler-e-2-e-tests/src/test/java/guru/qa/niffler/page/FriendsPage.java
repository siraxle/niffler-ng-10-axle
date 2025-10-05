package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class FriendsPage {
    private final SelenideElement searchInput = $x("//input[@aria-label='search']");
    private final ElementsCollection friendsCollection = $$x("//tbody[@id='friends']/tr");
//    private final By friendName = By.xpath(".//p[contains(@class, 'MuiTypography-body1')]");
    private final By unfriendBtn = By.xpath(".//button[text()='Unfriend']");
    private final By acceptBtn = By.xpath(".//button[text()='Accept']");
    private final By declineBtn = By.xpath(".//button[text()='Decline']");

    public FriendsPage removeFriend(String friendName) {
        SelenideElement friendRow = friendsCollection
                .find(text(friendName));
        friendRow.find(unfriendBtn).click();
        return this;
    }

    public FriendsPage verifyFriendRemoved(String friendName) {
        friendsCollection
                .findBy(text(friendName))
                .shouldNotBe(visible);
        return this;
    }

    public boolean isFriendNameExist(String friendName) {
        return friendsCollection
                .findBy(text(friendName))
                .is(visible);
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

    public boolean isRequestAccepted(String friendName) {
        SelenideElement row = friendsCollection.findBy(text(friendName));
        return row.find(acceptBtn).is(visible) &&
                row.find(declineBtn).is(visible);
    }

    public Integer getFriendsCount() {
        return friendsCollection.size();
    }

    public FriendsPage searchFriend(String friendName) {
        searchInput.setValue(friendName);
        return this;
    }

    public boolean hasIncomeRequest(String incomeFriendName) {
        return friendsCollection
                .findBy(text(incomeFriendName))
                .find(acceptBtn)
                .is(visible);
    }
}
