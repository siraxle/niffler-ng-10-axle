package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class MainPage {
    private final ElementsCollection tableRows = $$("#spendings tr");
    private final SelenideElement labelBtn = $x("//button[@aria-label='Menu']");
    private final SelenideElement profileBtn = $x("//a[@href='/profile']");
    private final SelenideElement friendsBtn = $x("//a[@href='/people/friends']");
    private final SelenideElement allPeopleBtn = $x("//a[@href='/people/all']");
    private final By searchInput = By.xpath(".//input[@placeholder='Search']");

    public EditSpendingPage editSpending(String description) {
        tableRows.find(text(description)).$$("td").get(5).click();
        return new EditSpendingPage();
    }

    public MainPage checkThatTableContains(String description) {
        tableRows.find(text(description)).should(visible);
        return this;
    }

    public MainPage checkThatMainPageContainsText(String... expectedTexts) {
        for (String text : expectedTexts) {
            $("body").shouldHave(text(text));
        }
        return this;
    }

    public UserProfilePage goToProfile() {
        labelBtn.click();
        profileBtn.click();
        return new UserProfilePage();
    }

    public FriendsPage goToFriendsPage() {
        labelBtn.click();
        friendsBtn.click();
        return new FriendsPage();
    }

    public AllPeoplePage goToAllPeoplePage() {
        labelBtn.click();
        allPeopleBtn.click();
        return new AllPeoplePage();
    }

    public MainPage search(String data) {
        $(searchInput).setValue(data).sendKeys(Keys.ENTER);
        return this;
    }
}