package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Selenide.$;

public class SearchField {

    private final SelenideElement searchInput;
    private final SelenideElement searchButton;

    public SearchField() {
        this.searchInput = $("input[aria-label='search']");
        this.searchButton = $("#input-submit");
    }

    public SearchField(SelenideElement searchInput, SelenideElement searchButton) {
        this.searchInput = searchInput;
        this.searchButton = searchButton;
    }

    @Step("Выполнить поиск: {query}")
    public SearchField search(String query) {
        searchInput.setValue(query);
        if (searchButton.exists()) {
            searchButton.click();
        } else {
            searchInput.sendKeys(Keys.ENTER);
        }
        return this;
    }

    @Step("Очистить поле поиска, если оно не пустое")
    public SearchField clearIfNotEmpty() {
        String currentValue = searchInput.getValue();
        if (currentValue != null && !currentValue.isEmpty()) {
            searchInput.clear();
            if (searchButton.exists()) {
                searchButton.click();
            } else {
                searchInput.sendKeys(Keys.ENTER);
            }
        }
        return this;
    }

    @Step("Очистить поле поиска")
    public SearchField clear() {
        searchInput.clear();
        return this;
    }

    @Step("Получить значение из поля поиска")
    public String getValue() {
        return searchInput.getValue();
    }

    @Step("Проверить, что поле поиска пустое")
    public SearchField shouldBeEmpty() {
        searchInput.shouldHave(com.codeborne.selenide.Condition.empty);
        return this;
    }
}