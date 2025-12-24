package guru.qa.niffler.page.component;

import com.codeborne.selenide.SelenideElement;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

@ParametersAreNonnullByDefault
public abstract class BaseComponent<T extends BaseComponent<T>> {

    private final SelenideElement menuButton = $x("//button[@aria-label='Menu']");

    @Nonnull
    public T menuShouldBeVisible() {
        menuButton.shouldBe(visible);
        return (T) this;
    }

    @Nonnull
    public T openMenu() {
        menuButton.click();
        return (T) this;
    }
}