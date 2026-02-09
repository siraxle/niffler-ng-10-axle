package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.Calendar;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@ParametersAreNonnullByDefault
public class EditSpendingPage extends BasePage<EditSpendingPage> {
    public static final String URL = CFG.frontUrl() + "spending";

    private final SelenideElement amountInput = $("#amount");
    private final SelenideElement currencySelect = $("#currency");
    private final SelenideElement categoryInput = $("#category");
    private final SelenideElement dateInput = $("input[name='date']");
    private final SelenideElement descriptionInput = $("#description");
    private final SelenideElement saveButton = $("#save");
    private final SelenideElement cancelButton = $("#cancel");
    private final SelenideElement calendarButton = $("button[aria-label*='Choose date']");
    private final ElementsCollection categoryOptions = $$(".MuiChip-root");

    public EditSpendingPage() {
        $("h2").shouldHave(
                text("Add new spending")
                        .or(text("Edit spending"))
        );
    }

    private Calendar calendar() {
        return new Calendar();
    }

    @Step("Установить сумму: {amount}")
    @Nonnull
    public EditSpendingPage setAmount(double amount) {
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    @Step("Установить сумму: {amount}")
    @Nonnull
    public EditSpendingPage setAmount(int amount) {
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    @Step("Установить валюту: {currency}")
    @Nonnull
    public EditSpendingPage setCurrency(String currency) {
        currencySelect.click();
        $$("li[role='option']").find(text(currency)).click();
        return this;
    }

    @Step("Ввести категорию {category}")
    @Nonnull
    public EditSpendingPage setCategory(String category) {
        categoryInput.setValue(category);
        return this;
    }

    @Step("Ввести описание: {description}")
    @Nonnull
    public EditSpendingPage setDescription(String description) {
        descriptionInput.setValue(description);
        return this;
    }

    @Step("Ввести описание новой траты: {description}")
    @Nonnull
    public EditSpendingPage setNewSpendingDescription(String description) {
        descriptionInput.val(description);
        return this;
    }

    @Step("Установить дату через календарь: {date}")
    @Nonnull
    public EditSpendingPage setDateViaCalendar(java.util.Date date) {
        calendarButton.click();
        calendar().selectDateInCalendar(date);
        return this;
    }

    @Step("Установить дату: {date}")
    @Nonnull
    public EditSpendingPage setDate(String date) {
        dateInput.setValue(date);
        return this;
    }

    @Step("Установить сегодняшнюю дату")
    @Nonnull
    public EditSpendingPage setTodayDate() {
        calendarButton.click();
        calendar().selectToday();
        return this;
    }

    @Step("Установить дату: {day}.{month + 1}.{year}")
    @Nonnull
    public EditSpendingPage setDate(int year, int month, int day) {
        calendarButton.click();
        calendar().selectDate(year, month, day);
        return this;
    }

    @Step("Сохранить трату")
    @Nonnull
    public MainPage save() {
        saveButton.click();
        return new MainPage();
    }

    @Step("Отменить создание/редактирование траты")
    @Nonnull
    public MainPage cancel() {
        cancelButton.click();
        return new MainPage();
    }
}