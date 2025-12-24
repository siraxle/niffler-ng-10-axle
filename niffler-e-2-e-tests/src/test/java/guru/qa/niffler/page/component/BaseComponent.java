package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.visible;

@ParametersAreNonnullByDefault
public abstract class BaseComponent<T extends BaseComponent<T>> {

    protected final SelenideElement self;

    protected BaseComponent(SelenideElement self) {
        this.self = self;
    }

    @Nonnull
    public T shouldBeVisible() {
        self.shouldBe(visible);
        return (T) this;
    }

    @Nonnull
    public T click() {
        self.click();
        return (T) this;
    }

    @Nonnull
    public String getText() {
        return self.getText();
    }

    @Nonnull
    public boolean isDisplayed() {
        return self.isDisplayed();
    }
}