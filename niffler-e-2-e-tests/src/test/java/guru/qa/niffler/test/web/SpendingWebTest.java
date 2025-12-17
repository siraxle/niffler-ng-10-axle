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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({BrowserExtension.class, UserExtension.class, SpendingExtension.class, CategoryExtension.class})
public class SpendingWebTest {

    private static final Config CFG = Config.getInstance();

    @User(
            username = "cat",
            spendings = @Spending(
                    category = "Учеба7",
                    amount = 89900,
                    currency = CurrencyValues.RUB,
                    description = "Обучение Niffler 2.0 юбилейный поток!"
            )
    )
    @Test
    void spendingDescriptionShouldBeEditedByTableAction(UserJson user) {
        SpendJson spending = user.testData().spendings().getFirst();
        final String newDescription = "Обучение Niffler Next Generation";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login(user.username(), user.testData().password())
                .checkThatTableContains(spending.description())
                .editSpending(spending.description())
                .setNewSpendingDescription(newDescription)
                .save()
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
                .checkThatTableContains(originalDescription)
                .editSpending(originalDescription)
                .setNewSpendingDescription(newDescription)
                .save()
                .checkThatTableContains(newDescription);
    }

}