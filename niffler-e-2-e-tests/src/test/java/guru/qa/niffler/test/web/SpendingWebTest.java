package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.*;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.utils.RandomDataUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BrowserExtension.class})
public class SpendingWebTest {

    private static final Config CFG = Config.getInstance();

    @User(
            spendings = @Spending(
                    amount = 50000,
                    description = "Исходное описание",
                    category = "Обучение"
            )
    )
    @Test
    void spendingDescriptionShouldBeEditedByTableAction(UserJson user) {
        SpendJson originalSpending = user.testData().spendings().getFirst();
        final String newDescription = "Обновлённое описание";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkThatTableContains(originalSpending.description())
                .editSpending(originalSpending.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .getSpendingTable()
                .checkThatTableContains(newDescription);
    }

    @User(
            spendings = @Spending(
                    amount = 89900,
                    description = "Исходное описание",
                    category = "Обучение"
            )
    )
    @Test
    void spendingDescriptionShouldBeEditedByTableAction3(UserJson user) {
        SpendJson originalSpending = user.testData().spendings().getFirst();
        final String originalDescription = originalSpending.description();
        final String newDescription = "Обновлённое описание после редактирования";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkThatTableContains(originalDescription)
                .editSpending(originalDescription)
                .setNewSpendingDescription(newDescription)
                .save()
                .getSpendingTable()
                .checkThatTableContains(newDescription);
    }


    @User(
            spendings = @Spending(
                    category = "Учеба",
                    amount = 89900,
                    currency = CurrencyValues.RUB,
                    description = "Обучение Niffler 2.0"
            )
    )
    @Test
    void spendingShouldBeVisibleInTable(UserJson user) {
        SpendJson spending = user.testData().spendings().getFirst();

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkThatTableContains(spending.description())
                .checkTableSize(1);
    }

    @User(
            spendings = {
                    @Spending(amount = 1000, description = "Обед", category = "Еда"),
                    @Spending(amount = 2000, description = "Кино", category = "Развлечения")
            }
    )
    @Test
    void shouldSearchSpendingByDescription(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkTableSize(2)
                .searchSpending("Обед")
                .checkTableSize(1)
                .checkThatTableContains("Обед")
                .checkThatTableContains("1000");
    }

    @User(
            spendings = @Spending(
                    amount = 10000,
                    description = "Тратa для удаления",
                    category = "Разное"
            )
    )
    @Test
    void shouldDeleteSpendingFromTable(UserJson user) {
        SpendJson spending = user.testData().spendings().getFirst();

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkThatTableContains(spending.description())
                .deleteSpending(spending.description())
                .checkTableSize(0);
    }

    @User(
            spendings = {
                    @Spending(amount = 1000, description = "Тратa 1", category = "Категория 1"),
                    @Spending(amount = 2000, description = "Тратa 2", category = "Категория 2"),
                    @Spending(amount = 3000, description = "Тратa 3", category = "Категория 3")
            }
    )
    @Test
    void shouldFilterSpendingsByPeriod(UserJson user) {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkTableSize(3)
                .selectPeriod("Last week")
                .checkTableSize(0);
    }

    @User(
            spendings = @Spending(
                    amount = 89900,
                    description = "Исходное описание",
                    category = "Обучение"
            )
    )
    @Test
    void spendingAlertShouldBeVisible(UserJson user) {
        SpendJson originalSpending = user.testData().spendings().getFirst();
        final String newDescription = "Обновлённое описание";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkThatTableContains(originalSpending.description())
                .editSpending(originalSpending.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .checkSnackbarText("Spending is edited successfully");

    }

}