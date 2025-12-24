package guru.qa.niffler.page.component;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.EditSpendingPage;
import io.qameta.allure.Step;
import lombok.Getter;
import org.openqa.selenium.By;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

@ParametersAreNonnullByDefault
public class SpendingTable {

    @Getter
    private final SearchField searchField = new SearchField();

    private static final By editButton = By.xpath(".//button[@aria-label='Edit spending']");
    private static final By checkBox = By.cssSelector("input[type='checkbox']");

    private final ElementsCollection spendingRows = $$("#spendings tbody tr");
    private final SelenideElement periodSelect = $("#period");
    private final SelenideElement deleteButton = $("#delete");
    private final SelenideElement previousPageButton = $("#page-prev");
    private final SelenideElement nextPageButton = $("#page-next");

    @Step("Проверить, что таблица содержит трату: {description}")
    @Nonnull
    public SpendingTable checkThatTableContains(String description) {
        spendingRows.findBy(text(description)).shouldBe(visible);
        return this;
    }

    @Step("Выбрать период: {period}")
    @Nonnull
    public SpendingTable selectPeriod(String period) {
        periodSelect.click();
        sleep(300);
        $x(String.format("//li[text()='%s']", period)).click();
        return this;
    }

    @Step("Редактировать трату: {description}")
    @Nonnull
    public EditSpendingPage editSpending(String description) {
        spendingRows.find(text(description))
                .find(editButton)
                .click();
        return new EditSpendingPage();
    }

    @Step("Удалить трату: {description}")
    @Nonnull
    public SpendingTable deleteSpending(String description) {
        selectSpending(description);
        deleteButton.shouldBe(visible).click();
        new DeleteSpendingDialog()
                .shouldBeVisible()
                .confirmDeletion();
        return this;
    }

    @Step("Отменить удаление траты: {description}")
    @Nonnull
    public SpendingTable cancelDeleteSpending(String description) {
        selectSpending(description);
        deleteButton.shouldBe(visible).click();
        new DeleteSpendingDialog()
                .shouldBeVisible()
                .cancelDeletion();
        return this;
    }

    @Step("Поиск траты: {str}")
    @Nonnull
    public SpendingTable searchSpending(String str) {
        searchField.search(str);
        return this;
    }

    @Step("Очистить поиск")
    @Nonnull
    public SpendingTable clearSearch() {
        searchField.clearIfNotEmpty();
        return this;
    }

    @Step("Проверить, что таблица содержит траты: {expectedSpends}")
    @Nonnull
    public SpendingTable checkTableContains(String... expectedSpends) {
        for (String spend : expectedSpends) {
            spendingRows.findBy(text(spend)).shouldBe(visible);
        }
        return this;
    }

    @Step("Проверить размер таблицы: ожидается {expectedSize}")
    @Nonnull
    public SpendingTable checkTableSize(int expectedSize) {
        spendingRows.shouldHave(size(expectedSize));
        return this;
    }

    @Step("Перейти на предыдущую страницу")
    @Nonnull
    public SpendingTable goToPreviousPage() {
        previousPageButton.shouldBe(visible).click();
        return this;
    }

    @Step("Перейти на следующую страницу")
    @Nonnull
    public SpendingTable goToNextPage() {
        nextPageButton.shouldBe(visible).click();
        return this;
    }

    @Step("Проверить кнопки навигации: предыдущая - {prevEnabled}, следующая - {nextEnabled}")
    @Nonnull
    public SpendingTable checkPageButtonsEnabled(boolean prevEnabled, boolean nextEnabled) {
        if (prevEnabled) {
            previousPageButton.shouldBe(visible);
        } else {
            previousPageButton.shouldNotBe(visible);
        }

        if (nextEnabled) {
            nextPageButton.shouldBe(visible);
        } else {
            nextPageButton.shouldNotBe(visible);
        }
        return this;
    }

    @Step("Выбрать трату: {description}")
    private void selectSpending(String description) {
        SelenideElement row = spendingRows.findBy(text(description));
        row.find(checkBox).click();
        deleteButton.shouldBe(visible);
    }
}