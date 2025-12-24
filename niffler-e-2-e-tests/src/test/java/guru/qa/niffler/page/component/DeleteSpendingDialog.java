package guru.qa.niffler.page.component;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class DeleteSpendingDialog {

    private final SelenideElement dialog = $(".MuiDialog-paper");
    private final SelenideElement cancelButton = dialog.$x(".//button[text()='Cancel']");
    private final SelenideElement deleteButton = dialog.$x(".//button[text()='Delete']");

    @Step("Проверить, что диалог удаления траты отображается")
    @Nonnull
    public DeleteSpendingDialog shouldBeVisible() {
        dialog.shouldBe(Condition.visible);
        dialog.shouldHave(text("Delete spendings?"));
        dialog.shouldHave(text("If you are sure, submit your action."));
        return this;
    }

    @Step("Нажать кнопку 'Cancel' в диалоге удаления")
    @Nonnull
    public DeleteSpendingDialog clickCancel() {
        cancelButton.click();
        dialog.shouldNotBe(Condition.visible);
        return this;
    }

    @Step("Нажать кнопку 'Delete' в диалоге удаления")
    @Nonnull
    public DeleteSpendingDialog clickDelete() {
        deleteButton.click();
        dialog.shouldNotBe(Condition.visible);
        return this;
    }

    @Step("Проверить, что диалог удаления не отображается")
    @Nonnull
    public DeleteSpendingDialog shouldNotBeVisible() {
        dialog.shouldNotBe(Condition.visible);
        return this;
    }

    @Step("Проверить текст заголовка диалога")
    @Nonnull
    public DeleteSpendingDialog verifyTitle(String expectedTitle) {
        dialog.shouldHave(text(expectedTitle));
        return this;
    }

    @Step("Проверить текст описания диалога")
    @Nonnull
    public DeleteSpendingDialog verifyDescription(String expectedDescription) {
        dialog.shouldHave(text(expectedDescription));
        return this;
    }

    @Step("Подтвердить удаление траты")
    public void confirmDeletion() {
        shouldBeVisible();
        clickDelete();
        shouldNotBeVisible();
    }

    @Step("Отменить удаление траты")
    public void cancelDeletion() {
        shouldBeVisible();
        clickCancel();
        shouldNotBeVisible();
    }
}