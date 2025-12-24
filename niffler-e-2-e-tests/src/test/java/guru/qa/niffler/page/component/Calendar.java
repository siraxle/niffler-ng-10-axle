package guru.qa.niffler.page.component;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.time.Month;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.codeborne.selenide.Condition.matchText;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static java.util.Calendar.*;

@ParametersAreNonnullByDefault
public class Calendar {

    private final SelenideElement calendarButton = $("button[aria-label*='Choose date']");
    private final SelenideElement previousMonthBtn = $("button[title='Previous month']");
    private final SelenideElement nextMonthBtn = $("button[title='Next month']");
    private final SelenideElement currentDateLabel = $(".MuiPickersCalendarHeader-label");
    private final ElementsCollection dateRows = $$(".MuiDayCalendar-weekContainer");

    @Step("Выбрать дату: {date}")
    @Nonnull
    public Calendar selectDateInCalendar(Date date) {
        java.util.Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        calendarButton.click();
        selectYear(calendar.get(YEAR));
        selectMonth(calendar.get(MONTH));
        selectDay(calendar.get(DAY_OF_MONTH));
        return this;
    }

    @Step("Выбрать год: {year}")
    private void selectYear(int year) {
        String[] dateParts = getDateFromComponent();
        int actualYear = Integer.parseInt(dateParts[1]);

        while (actualYear > year) {
            previousMonthBtn.click();
            actualYear = Integer.parseInt(getDateFromComponent()[1]);
        }

        while (actualYear < year) {
            nextMonthBtn.click();
            actualYear = Integer.parseInt(getDateFromComponent()[1]);
        }
    }

    @Step("Выбрать месяц: {month}")
    private void selectMonth(int month) {
        String[] dateParts = getDateFromComponent();
        int actualMonth = Month.valueOf(dateParts[0].toUpperCase()).ordinal();

        while (actualMonth > month) {
            previousMonthBtn.click();
            actualMonth = Month.valueOf(getDateFromComponent()[0].toUpperCase()).ordinal();
        }

        while (actualMonth < month) {
            nextMonthBtn.click();
            actualMonth = Month.valueOf(getDateFromComponent()[0].toUpperCase()).ordinal();
        }
    }

    @Step("Выбрать день: {day}")
    private void selectDay(int day) {
        for (SelenideElement row : dateRows.snapshot()) {
            ElementsCollection days = row.$$("button").snapshot();
            for (SelenideElement d : days) {
                if (d.getText().equals(String.valueOf(day))) {
                    d.click();
                    return;
                }
            }
        }
    }

    @Nonnull
    private String[] getDateFromComponent() {
        return currentDateLabel.should(matchText(".*\\d{4}"))
                .getText()
                .split(" ");
    }

    @Step("Выбрать сегодняшнюю дату")
    @Nonnull
    public Calendar selectToday() {
        return selectDateInCalendar(new Date());
    }

    @Step("Выбрать дату: {day}.{month + 1}.{year}")
    @Nonnull
    public Calendar selectDate(int year, int month, int day) {
        java.util.Calendar calendar = new GregorianCalendar(year, month, day);
        return selectDateInCalendar(calendar.getTime());
    }

    @Step("Открыть календарь")
    @Nonnull
    public Calendar open() {
        calendarButton.click();
        return this;
    }
}