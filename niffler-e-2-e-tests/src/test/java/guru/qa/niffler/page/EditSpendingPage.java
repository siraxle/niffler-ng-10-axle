package guru.qa.niffler.page;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import guru.qa.niffler.page.component.Calendar;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class EditSpendingPage {

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
    public EditSpendingPage setAmount(double amount) {
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    @Step("Установить сумму: {amount}")
    public EditSpendingPage setAmount(int amount) {
        amountInput.setValue(String.valueOf(amount));
        return this;
    }

    @Step("Установить валюту: {currency}")
    public EditSpendingPage setCurrency(String currency) {
        currencySelect.click();
        $$("li[role='option']").find(text(currency)).click();
        return this;
    }

    @Step("Ввести категорию {category}")
    public @Nonnull EditSpendingPage setCategory(String category) {
        categoryInput.setValue(category);
        return this;
    }

    @Step("Ввести описание: {description}")
    public EditSpendingPage setDescription(String description) {
        descriptionInput.setValue(description);
        return this;
    }

    @Step("Ввести описание новой траты: {description}")
    public EditSpendingPage setNewSpendingDescription(String description) {
        descriptionInput.val(description);
        return this;
    }

    @Step("Установить дату через календарь: {date}")
    public EditSpendingPage setDateViaCalendar(java.util.Date date) {
        // Используем кнопку календаря на странице
        calendarButton.click();
        calendar().selectDateInCalendar(date);
        return this;
    }

    @Step("Установить дату: {date}")
    public EditSpendingPage setDate(String date) {
        dateInput.setValue(date);
        return this;
    }

    @Step("Установить сегодняшнюю дату")
    public EditSpendingPage setTodayDate() {
        calendarButton.click();
        calendar().selectToday();
        return this;
    }

    @Step("Установить дату: {day}.{month + 1}.{year}")
    public EditSpendingPage setDate(int year, int month, int day) {
        calendarButton.click();
        calendar().selectDate(year, month, day);
        return this;
    }

    @Step("Сохранить трату")
    public MainPage save() {
        saveButton.click();
        return new MainPage();
    }

    @Step("Отменить создание/редактирование траты")
    public MainPage cancel() {
        cancelButton.click();
        return new MainPage();
    }
}