package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$x;

public class AllPeoplePage {
    private final ElementsCollection allPeopleTable = $$x("//tbody[@id='all']/tr");
    private final By waitingFlag = By.xpath(".//span[text() = 'Waiting...']");
    private final By searchInput = By.xpath(".//input[@placeholder='Search']");

    public AllPeoplePage hasOutcomeRequest(String targetUserName) {
        SelenideElement requestRow = allPeopleTable.findBy(text(targetUserName));
        requestRow.find(waitingFlag).shouldBe(visible);
        return this;
    }


    public AllPeoplePage searchPeople(String outcomeInvitationUsername) {
        $(searchInput).setValue(outcomeInvitationUsername).sendKeys(Keys.ENTER);
        return this;
    }
}
