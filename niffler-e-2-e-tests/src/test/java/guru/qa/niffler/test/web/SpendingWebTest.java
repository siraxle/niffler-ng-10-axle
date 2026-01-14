package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.ScreenShotTest;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.model.UserJson;
import guru.qa.niffler.page.LoginPage;
import guru.qa.niffler.page.MainPage;
import guru.qa.niffler.utils.ScreenDiffResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            spendings = @Spending(
                    amount = 89900,
                    description = "Исходное описание",
                    category = "Обучение"
            )
    )
    @Test
    @ScreenShotTest("img/expected-stat.png")
    void checkStatComponentTest(UserJson user, BufferedImage expected) throws IOException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());
        Selenide.sleep(3000);
        BufferedImage actual = ImageIO.read($("canvas[role='img']").screenshot());

        assertFalse(new ScreenDiffResult(expected, actual));
    }

    @User(
            spendings = @Spending(
                    amount = 89900,
                    description = "Исходное описание",
                    category = "Обучение"
            )
    )
    @Test
    @ScreenShotTest("img/expected-stat.png")
    void checkAvatarComponentTest(UserJson user, BufferedImage expected) throws IOException {
        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());
        Selenide.sleep(3000);
        BufferedImage actual = ImageIO.read($x("(//div[contains(@class, 'MuiAvatar-root')])[2]").screenshot());

        assertFalse(new ScreenDiffResult(expected, actual));
    }

    @User(
            spendings =
                    {@Spending(amount = 1000, description = "Тратa 1", category = "Категория 1"),
                    @Spending(amount = 2000, description = "Тратa 2", category = "Категория 2")}
    )
    @ScreenShotTest("img/expected-stat-after-del.png")
    void checkRemovingStatComponentTest(UserJson user, BufferedImage expected) throws IOException {
        MainPage mainPage = Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());
        Selenide.sleep(3000);
        mainPage.verifyLegendElementsSize(2);
        mainPage.verifyLegendElementTextByIndex(0, "Категория 2 2000 ₽");
        mainPage.verifyLegendElementTextByIndex(1, "Категория 1 1000 ₽");

        BufferedImage before = ImageIO.read($("canvas[role='img']").screenshot());
        mainPage.selectCheckboxByIndex(1).clickRemoveButton();
        mainPage.clickConfirmDeleteInDialog();
        Selenide.sleep(3000);
        BufferedImage after = ImageIO.read($("canvas[role='img']").screenshot());
        mainPage.verifyLegendElementsSize(1).verifyLegendElementTextByIndex(0, "Категория 2 2000 ₽");
        assertTrue(new ScreenDiffResult(before, after));
        assertFalse(new ScreenDiffResult(after, expected));
    }

    @User(
            spendings =
                    {@Spending(amount = 1000, description = "Тратa 1", category = "Категория 1")}
    )
    @ScreenShotTest("img/expected-stat-after-edit.png")
    void checkEditStatComponentTest(UserJson user, BufferedImage expected) throws IOException {
        MainPage mainPage = Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());
        Selenide.sleep(3000);
        BufferedImage before = ImageIO.read($("canvas[role='img']").screenshot());
        mainPage.verifyLegendElementsSize(1).verifyLegendElementTextByIndex(0, "Категория 1 1000 ₽");

        mainPage.clickEditBtn().setAmount(100000).save();
        Selenide.sleep(3000);
        BufferedImage after = ImageIO.read($("canvas[role='img']").screenshot());
        mainPage.verifyLegendElementsSize(1).verifyLegendElementTextByIndex(0, "Категория 1 100000 ₽");
        assertTrue(new ScreenDiffResult(before, after));
        assertFalse(new ScreenDiffResult(after, expected));
    }

    @User(
            spendings =
                    {@Spending(amount = 1000, description = "Тратa 1", category = "Категория 1"),
                            @Spending(amount = 2000, description = "Тратa 2", category = "Категория 2")}
    )
    @ScreenShotTest(value = "img/expected-stat-after-archive.png", rewriteExpected = true)
    void checkAfterArchivedStatComponentTest(UserJson user, BufferedImage expected) throws IOException {
        MainPage mainPage = Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password());
        Selenide.sleep(3000);
        mainPage.verifyLegendElementsSize(2);
        mainPage.verifyLegendElementTextByIndex(0, "Категория 2 2000 ₽");
        mainPage.verifyLegendElementTextByIndex(1, "Категория 1 1000 ₽");

        BufferedImage before = ImageIO.read($("canvas[role='img']").screenshot());
        mainPage.getHeader().toProfilePage()
                .archiveCategory("Категория 1")
                .saveChanges();
        Selenide.open(CFG.frontUrl(), LoginPage.class);

        Selenide.sleep(3000);
        BufferedImage after = ImageIO.read($("canvas[role='img']").screenshot());
        mainPage.verifyLegendElementTextByIndex(0, "Категория 2 2000 ₽");
        mainPage.verifyLegendElementTextByIndex(1, "Archived 1000 ₽");
        assertTrue(new ScreenDiffResult(before, after));
        assertFalse(new ScreenDiffResult(after, expected));
    }




}