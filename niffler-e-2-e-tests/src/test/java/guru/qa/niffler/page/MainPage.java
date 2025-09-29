package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class MainPage {
  private final ElementsCollection tableRows = $$("#spendings tr");
  private final SelenideElement showArchivedSwitch = $("input[type='checkbox']");
  private final SelenideElement labelBtn = $x("//button[@aria-label='Menu']");
  private final SelenideElement profileBtn = $x("//a[@href='/profile']");

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
    labelBtn.shouldBe(visible).click();
    profileBtn.shouldBe(visible).click();
    return new UserProfilePage();
  }
}
