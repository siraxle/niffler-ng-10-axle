package guru.qa.niffler.page;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.Header;
import guru.qa.niffler.page.component.SpendingTable;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

@ParametersAreNonnullByDefault
public class MainPage extends BasePage<MainPage> {

    private final Header header = new Header();
    private final SpendingTable spendingTable = new SpendingTable();
    private final SelenideElement removeBtn = $x("//button[@id='delete']");
    private final SelenideElement editBtn = $x("//button[@aria-label='Edit spending']");
    private final ElementsCollection checkBoxes = $$("input[type='checkbox'][class*='PrivateSwitchBase-input']");
    private final SelenideElement confirmDeleteButton = $x("//div[@aria-describedby='alert-dialog-slide-description']//button[contains(text(), 'Delete')]");
    private final SelenideElement cancelButton = $x("//div[@aria-describedby='alert-dialog-slide-description']//button[contains(text(), 'Cancel')]");
    private final ElementsCollection legendElements = $$x("//div[@id='legend-container']//li");


    @Step("Получить компонент Header")
    @Nonnull
    public Header getHeader() {
        return header;
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

    @Step("Кликнуть на кнопку удаления")
    @Nonnull
    public MainPage clickRemoveButton() {
        removeBtn.click();
        return this;
    }

    @Step("Выбрать чекбокс под номером: {index}")
    @Nonnull
    public MainPage selectCheckboxByIndex(int index) {
        checkBoxes.get(index).click();
        return this;
    }

    @Step("Нажать кнопку Cancel в модальном окне")
    @Nonnull
    public MainPage clickCancelInDialog() {
        cancelButton.click();
        return this;
    }

    @Step("Нажать кнопку Delete в модальном окне (подтвердить удаление)")
    @Nonnull
    public MainPage clickConfirmDeleteInDialog() {
        confirmDeleteButton.click();
        return this;
    }

    @Step("Проверить размер элементов легенды: ожидается {expectedSize}")
    @Nonnull
    public MainPage verifyLegendElementsSize(int expectedSize) {
        legendElements.shouldHave(CollectionCondition.size(expectedSize));
        return this;
    }

    @Step("Проверить, что конкретный элемент легенды по индексу {index} содержит текст: {expectedText}")
    @Nonnull
    public MainPage verifyLegendElementTextByIndex(int index, String expectedText) {
        String text = legendElements.get(index).text();
        System.out.println(text);
        legendElements.get(index).shouldHave(Condition.text(expectedText));
        return this;
    }

    @Step("Нажать кнопку правки траты")
    @Nonnull
    public EditSpendingPage clickEditBtn() {
        editBtn.click();
        return new EditSpendingPage();
    }



}