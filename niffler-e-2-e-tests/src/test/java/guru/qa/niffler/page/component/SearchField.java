package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class SearchField extends BaseComponent<SearchField> {

    public SearchField(@Nonnull SelenideElement self) {
        super(self);
    }

    public SearchField() {
        super($("input[aria-label='search']"));
    }

    @Step("Выполнить поиск: {query}")
    @Nonnull
    public SearchField search(String query) {
        self.setValue(query);
        self.sendKeys(Keys.ENTER);
        return this;
    }

    @Step("Очистить поле поиска, если оно не пустое")
    @Nonnull
    public SearchField clearIfNotEmpty() {
        String currentValue = self.getValue();
        if (currentValue != null && !currentValue.isEmpty()) {
            self.clear();
            self.sendKeys(Keys.ENTER);
        }
        return this;
    }

    @Step("Очистить поле поиска")
    @Nonnull
    public SearchField clear() {
        self.clear();
        return this;
    }

    @Step("Получить значение из поля поиска")
    @Nonnull
    public String getValue() {
        return self.getValue();
    }

    @Step("Проверить, что поле поиска пустое")
    @Nonnull
    public SearchField shouldBeEmpty() {
        self.shouldHave(empty);
        return this;
    }
}