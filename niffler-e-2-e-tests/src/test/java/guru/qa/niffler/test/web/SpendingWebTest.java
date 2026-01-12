package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.condition.Bubble;
import guru.qa.niffler.condition.Color;
import guru.qa.niffler.condition.StatConditions;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.component.StatComponent;
import guru.qa.niffler.utils.ScreenDiffResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

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

    @User(
            spendings = {@Spending(
                    amount = 89900,
                    description = "Исходное описание",
                    category = "Обучение"
            ),
                    @Spending(
                            amount = 1000,
                            description = "Рыбалка",
                            category = "Рыбалка на Неве"
                    )
            }
    )
    @Test
    @ScreenShotTest("img/expected-stat-with-2-categories.png")
    void checkStatComponentTest(UserJson user, BufferedImage expected) throws IOException, InterruptedException {
        StatComponent statComponent = Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getStatComponent();

        Thread.sleep(3000);

        statComponent.checkBubbles(
                new Bubble(Color.yellow, "Обучение 89900 ₽"),
                new Bubble(Color.green, "Рыбалка на Неве 1000 ₽")
        );
        statComponent.checkBubblesInAnyOrder(
                new Bubble(Color.green, "Рыбалка на Неве 1000 ₽"),
                new Bubble(Color.yellow, "Обучение 89900 ₽")  // Порок изменён
        );

        statComponent.checkBubblesContains(
                new Bubble(Color.yellow, "Обучение 89900 ₽")
        );

        assertFalse(new ScreenDiffResult(
                        expected,
                        statComponent.chartScreenshot()),
                "Screen comparison failure");

    }

    @User(
            spendings = {
                    @Spending(
                            category = "Обучение",
                            amount = 50000,
                            currency = CurrencyValues.RUB,
                            description = "Курс по Java"
                    ),
                    @Spending(
                            category = "Еда",
                            amount = 1500.50,
                            currency = CurrencyValues.RUB,
                            description = "Обед в кафе"
                    )
            }
    )
    @Test
    void shouldCheckSpendsInTable(UserJson user) {
        SpendJson spend1 = user.testData().spendings().get(0);
        SpendJson spend2 = user.testData().spendings().get(1);

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .checkThatTableContains(spend1.description())
                .getTableRows()
                .should(StatConditions.spends(spend1, spend2));
    }

    @User(
            spendings = @Spending(
                    category = "Путешествия",
                    amount = 100000,
                    currency = CurrencyValues.RUB,
                    description = "Поездка в Сочи"
            )
    )
    @Test
    void shouldCheckSingleSpendInTable(UserJson user) {
        SpendJson expectedSpend = user.testData().spendings().getFirst();

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .getTableRows()
                .should(StatConditions.spends(expectedSpend));
    }

    @User(
            spendings = {
                    @Spending(
                            category = "Обучение",
                            amount = 50000,
                            currency = CurrencyValues.RUB,
                            description = "Курс по Java"
                    ),
                    @Spending(
                            category = "Еда",
                            amount = 1500.50,
                            currency = CurrencyValues.EUR,
                            description = "Обед в кафе"
                    ),
                    @Spending(
                            category = "Кино",
                            amount = 1600.51,
                            currency = CurrencyValues.KZT,
                            description = "Первому игроку приготовиться"
                    ),
                    @Spending(
                            category = "ЖКХ",
                            amount = 1700.52,
                            currency = CurrencyValues.USD,
                            description = "Холодная вода"
                    )
            }
    )
    @Test
    void shouldCheckAllSpendsInTable(UserJson user) {
        List<SpendJson> allSpends = user.testData().spendings();

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .getSpendingTable()
                .getTableRows()
                .should(StatConditions.spends(
                        allSpends.get(0),
                        allSpends.get(1),
                        allSpends.get(2),
                        allSpends.get(3)
                ));
    }

}