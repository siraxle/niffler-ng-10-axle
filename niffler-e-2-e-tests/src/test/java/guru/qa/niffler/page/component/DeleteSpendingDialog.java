package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

@ParametersAreNonnullByDefault
public class DeleteSpendingDialog extends BaseComponent<DeleteSpendingDialog> {

    private final SelenideElement cancelButton;
    private final SelenideElement deleteButton;

    public DeleteSpendingDialog(@Nonnull SelenideElement self) {
        super(self);
        this.cancelButton = self.$x(".//button[text()='Cancel']");
        this.deleteButton = self.$x(".//button[text()='Delete']");
    }

    public DeleteSpendingDialog() {
        super($(".MuiDialog-paper"));
        this.cancelButton = self.$x(".//button[text()='Cancel']");
        this.deleteButton = self.$x(".//button[text()='Delete']");
    }

    @Step("Проверить, что диалог удаления траты отображается")
    @Nonnull
    public DeleteSpendingDialog shouldBeVisible() {
        self.shouldBe(visible);
        self.shouldHave(text("Delete spendings?"));
        self.shouldHave(text("If you are sure, submit your action."));
        return this;
    }

    @Step("Нажать кнопку 'Cancel' в диалоге удаления")
    @Nonnull
    public DeleteSpendingDialog clickCancel() {
        cancelButton.click();
        return this;
    }

    @Step("Нажать кнопку 'Delete' в диалоге удаления")
    @Nonnull
    public DeleteSpendingDialog clickDelete() {
        deleteButton.click();
        return this;
    }

    @Step("Проверить текст заголовка диалога")
    @Nonnull
    public DeleteSpendingDialog verifyTitle(String expectedTitle) {
        self.shouldHave(text(expectedTitle));
        return this;
    }

    @Step("Проверить текст описания диалога")
    @Nonnull
    public DeleteSpendingDialog verifyDescription(String expectedDescription) {
        self.shouldHave(text(expectedDescription));
        return this;
    }

    @Step("Подтвердить удаление траты")
    @Nonnull
    public DeleteSpendingDialog confirmDeletion() {
        shouldBeVisible();
        clickDelete();
        return this;
    }

    @Step("Отменить удаление траты")
    @Nonnull
    public DeleteSpendingDialog cancelDeletion() {
        shouldBeVisible();
        clickCancel();
        return this;
    }
}