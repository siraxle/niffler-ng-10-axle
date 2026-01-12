package guru.qa.niffler.page;

import guru.qa.niffler.page.component.Header;
import guru.qa.niffler.page.component.SpendingTable;
import guru.qa.niffler.page.component.StatComponent;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class MainPage extends BasePage<MainPage> {

    private final Header header = new Header();
    private final SpendingTable spendingTable = new SpendingTable();
    private final StatComponent statComponent = new StatComponent();

    @Step("Получить компонент Header")
    @Nonnull
    public Header getHeader() {
        return header;
    }

    @Nonnull
    public StatComponent getStatComponent() {
        statComponent.getSelf().scrollIntoView(true);
        return  statComponent;
    }

    @Step("Получить таблицу трат")
    @Nonnull
    public SpendingTable getSpendingTable() {
        return spendingTable;
    }

    @Step("Проверить, что главная страница содержит текст: {expectedTexts}")
    @Nonnull
    public MainPage checkThatMainPageContainsText(String... expectedTexts) {
        for (String text : expectedTexts) {
            $("body").shouldHave(text(text));
        }
        return this;
    }
}