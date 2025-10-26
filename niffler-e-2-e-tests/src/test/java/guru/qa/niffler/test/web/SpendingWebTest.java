package guru.qa.niffler.test.web;

import com.codeborne.selenide.Selenide;
import guru.qa.niffler.config.Config;
import guru.qa.niffler.jupiter.annotation.Spending;
import guru.qa.niffler.jupiter.annotation.User;
import guru.qa.niffler.jupiter.extension.BrowserExtension;
import guru.qa.niffler.jupiter.extension.TestMethodContextExtension;
import guru.qa.niffler.model.CurrencyValues;
import guru.qa.niffler.model.SpendJson;
import guru.qa.niffler.page.LoginPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@ExtendWith(BrowserExtension.class)
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
//    @DisableByIssue("2")
    @Test
    void spendingDescriptionShouldBeEditedByTableAction(SpendJson spending) {
        ExtensionContext ctx = TestMethodContextExtension.context();

        final String newDescription = "Обучение Niffler Next Generation";

        Selenide.open(CFG.frontUrl(), LoginPage.class)
                .login("cat", "123456")
                .editSpending(spending.description())
                .setNewSpendingDescription(newDescription)
                .save()
                .checkThatTableContains(newDescription);
    }
}