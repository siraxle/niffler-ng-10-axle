package guru.qa.niffler.page;

import guru.qa.niffler.page.component.Header;
import guru.qa.niffler.page.component.SpendingTable;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class MainPage {

    private final Header header = new Header();
    private final SpendingTable spendingTable = new SpendingTable();

    @Step("Получить компонент Header")
    public Header getHeader() {
        return header;
    }

    @Step("Получить таблицу трат")
    public SpendingTable getSpendingTable() {
        return spendingTable;
    }

    @Step("Проверить, что главная страница содержит текст: {expectedTexts}")
    public MainPage checkThatMainPageContainsText(String... expectedTexts) {
        for (String text : expectedTexts) {
            $("body").shouldHave(text(text));
        }
        return this;
    }
}